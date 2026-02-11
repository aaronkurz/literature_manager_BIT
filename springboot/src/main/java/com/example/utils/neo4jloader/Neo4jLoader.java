package com.example.utils.neo4jloader;

import org.neo4j.driver.Driver;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import com.example.utils.Config;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Neo4jLoader {

    private static Driver driver;

    public Neo4jLoader(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        LogUtil_Neo4jLoader.log("Neo4j connection established: " + uri);
    }

    public void close() {
        driver.close();
        LogUtil_Neo4jLoader.log("Neo4j connection closed");
    }

    public void loadDataFromMySQL(String jdbcUrl, String jdbcUser, String jdbcPassword, String title, boolean ifDeleteAllNodeFirst) {
        LogUtil_Neo4jLoader.log("Loading data from MySQL: " + jdbcUrl);
        try (Connection mysqlConnection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            LogUtil_Neo4jLoader.log("MySQL connection established: " + jdbcUrl);

            // Build query dynamically
            String query = ifDeleteAllNodeFirst
                    ? "SELECT * FROM article_info"
                    : "SELECT * FROM article_info WHERE Title = ?";

            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                // Set parameter for conditional query
                if (!ifDeleteAllNodeFirst) {
                    statement.setString(1, title);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<String> cleanedHeaders = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        cleanedHeaders.add(metaData.getColumnLabel(i).trim());
                    }
                    LogUtil_Neo4jLoader.log("MySQL table structure: " + cleanedHeaders);

                    try (Session session = driver.session()) {
                        while (resultSet.next()) {
                            Map<String, String> rowMap = new HashMap<>();
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnLabel(i);
                                String columnValue = resultSet.getString(i);
                                String value = columnValue == null ? "" : columnValue.trim();
                                // store both original and Capitalized key to handle case differences
                                rowMap.put(columnName, value);
                                if (columnName.length() > 0) {
                                    String cap = columnName.substring(0,1).toUpperCase() + columnName.substring(1);
                                    rowMap.put(cap, value);
                                }
                            }
                            boolean shouldContinue = processRecord(session, rowMap);
                            if (!shouldContinue) {
                                LogUtil_Neo4jLoader.log("Existing data detected, stopping import");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil_Neo4jLoader.log("MySQL connection or query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processRecord(Session session, Map<String, String> row) {
            System.out.println("=== Processing record ===");
            System.out.println("Row keys: " + row.keySet());
            System.out.println("Checking custom_concept fields:");
            System.out.println("  custom_concept1: " + (row.containsKey("custom_concept1") ? "exists - " + row.get("custom_concept1") : "not found"));
            System.out.println("  custom_concept2: " + (row.containsKey("custom_concept2") ? "exists - " + row.get("custom_concept2") : "not found"));
            System.out.println("  custom_concept3: " + (row.containsKey("custom_concept3") ? "exists - " + row.get("custom_concept3") : "not found"));
            String title = row.getOrDefault("Title", "").trim();
            System.out.println("Resolved title: '" + title + "'");
        if (title.isEmpty()) {
            LogUtil_Neo4jLoader.log("Record has no title, skipping");
            return true;
        }

        // Extract author list (limit 5)
        List<String> authors = Arrays.stream(row.getOrDefault("Author", "").split(";"))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .limit(5)  // Limit to 5 authors
                .sorted()
                .collect(Collectors.toList());

        // Check for existing paper with same title and authors
        boolean exists = checkPaperExists(session, title, authors);
        if (exists) {
            LogUtil_Neo4jLoader.log("Duplicate paper detected: title='" + title + "', authors=" + authors);
            return false;
        }

        // Create new paper node
        Long paperId = createPaper(session, row);
        LogUtil_Neo4jLoader.log("Paper node created: ID=" + paperId + ", title=" + title);

        // Process author relationships (limit 5)
        String[] authorArray = row.getOrDefault("Author", "").split(";");
        int authorCount = 0;
        for (String author : authorArray) {
            author = author.trim();
            if (!author.isEmpty() && authorCount < 5) {
                createAuthorRelationship(session, paperId, author, row);
                LogUtil_Neo4jLoader.log("Author relationship created: paperID=" + paperId + ", author=" + author);
                authorCount++;
            }
        }

        // Process other relationships (keywords, funds, etc.)
        processRelationships(session, paperId, row);

        return true;
    }

    private boolean checkPaperExists(Session session, String title, List<String> authorList) {
        String query = "MATCH (p:Paper {title: $title})-[:AUTHORED_BY]->(a:Author) " +
                "WITH p, collect(a.name) AS dbAuthors " +
                "WHERE dbAuthors = $sortedAuthors " +
                "RETURN count(p) > 0 as exists";

        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("sortedAuthors", authorList.stream().sorted().collect(Collectors.toList()));

        try {
            Result result = session.run(query, params);
            return result.single().get("exists").asBoolean();
        } catch (NoSuchRecordException e) {
            return false;
        }
    }

    private Long createPaper(Session session, Map<String, String> row) {
        String query = "CREATE (p:Paper {title: $title, doi: $doi, abstract: $abstract, " +
                "pubDate: $pubDate, year: $year, pages: $pages, url: $url, " +
                "created_at: timestamp()}) RETURN id(p) as id";
        Map<String, Object> params = new HashMap<>();
        params.put("title", row.getOrDefault("Title", ""));
        params.put("doi", row.getOrDefault("DOI", ""));
        params.put("abstract", row.getOrDefault("Summary", ""));
        params.put("pubDate", row.getOrDefault("PubTime", ""));
        params.put("year", row.getOrDefault("Year", ""));
        params.put("pages", row.getOrDefault("PageCount", ""));
        params.put("url", row.getOrDefault("URL", ""));

        Result result = session.run(query, params);
        return result.single().get("id").asLong();
    }

    private void processRelationships(Session session, Long paperId, Map<String, String> row) {
        // Process keywords
        String[] keywords = row.getOrDefault("Keyword", "").split(";");
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (!keyword.isEmpty()) {
                createKeywordRelationship(session, paperId, keyword);
                LogUtil_Neo4jLoader.log("Keyword relationship created: paperID=" + paperId + ", keyword=" + keyword);
            }
        }

        // Process funds
        String[] funds = row.getOrDefault("Fund", "").split(";");
        for (String fund : funds) {
            fund = fund.trim();
            if (!fund.isEmpty()) {
                createFundRelationship(session, paperId, fund);
                LogUtil_Neo4jLoader.log("Fund relationship created: paperID=" + paperId + ", fund=" + fund);
            }
        }

        // Process categories
        String[] clcs = row.getOrDefault("CLC", "").split(";");
        for (String clc : clcs) {
            clc = clc.trim();
            if (!clc.isEmpty()) {
                createClcRelationship(session, paperId, clc);
                LogUtil_Neo4jLoader.log("Category relationship created: paperID=" + paperId + ", category=" + clc);
            }
        }

        // Process source DB
        String srcDatabase = row.get("SrcDatabase");
        if (srcDatabase != null && !srcDatabase.trim().isEmpty()) {
            createSrcDatabaseRelationship(session, paperId, srcDatabase.trim());
            LogUtil_Neo4jLoader.log("SourceDB relationship created: paperID=" + paperId + ", sourceDB=" + srcDatabase);
        }
        
        // Process custom concepts
        processCustomConcepts(session, paperId, row);
    }
    
    /**
     * Process custom concepts and create relationships
     */
    private void processCustomConcepts(Session session, Long paperId, Map<String, String> row) {
        System.out.println("=== Processing custom concepts ===");
        System.out.println("Paper ID: " + paperId);
        
        for (int i = 1; i <= 3; i++) {
            String customConceptKey = "custom_concept" + i;
            String customConceptJson = row.get(customConceptKey);
            
            System.out.println("Checking custom concept field: " + customConceptKey + ", value: " + 
                (customConceptJson == null ? "NULL" : (customConceptJson.isEmpty() ? "EMPTY" : customConceptJson)));
            
            if (customConceptJson == null || customConceptJson.trim().isEmpty()) {
                continue;
            }
            
            try {
                // Parse JSON: {"relationshipName": "method", "matchingConcepts": ["RCT", "Cohort"]}
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(customConceptJson).getAsJsonObject();
                
                if (!json.has("relationshipName") || !json.has("matchingConcepts")) {
                    System.err.println("JSON missing required fields: " + customConceptJson);
                    continue;
                }
                
                // Check for null values
                if (json.get("relationshipName").isJsonNull() || json.get("matchingConcepts").isJsonNull()) {
                    System.err.println("JSON field is null: " + customConceptJson);
                    continue;
                }
                
                String relationshipName = json.get("relationshipName").getAsString();
                com.google.gson.JsonArray concepts = json.getAsJsonArray("matchingConcepts");
                
                System.out.println("Parsed custom concept: relationship=" + relationshipName + ", conceptCount=" + concepts.size());
                
                if (relationshipName == null || relationshipName.trim().isEmpty() || concepts.size() == 0) {
                    System.err.println("Relationship name is empty or concept list is empty");
                    continue;
                }
                
                // Create relationship for each matching concept
                for (int j = 0; j < concepts.size(); j++) {
                    if (!concepts.get(j).isJsonNull()) {
                        String conceptValue = concepts.get(j).getAsString();
                        if (conceptValue != null && !conceptValue.trim().isEmpty()) {
                            createCustomConceptRelationship(session, paperId, relationshipName, conceptValue);
                            System.out.println("Custom concept relationship created: paperID=" + paperId + 
                                ", relationship=" + relationshipName + ", concept=" + conceptValue);
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Failed to parse custom concept: " + customConceptKey + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("=== Custom concepts processing completed ===");
    }
    
    /**
     * Create custom concept relationship
     */
    private void createCustomConceptRelationship(Session session, Long paperId, String relationshipName, String conceptValue) {
        // Using dynamic relationship type is not directly supported in Cypher, 
        // so we'll create a generic CUSTOM_CONCEPT relationship with properties
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (c:CustomConcept {name: $conceptValue, relationship: $relationshipName}) " +
                "MERGE (p)-[r:HAS_CUSTOM_CONCEPT]->(c) " +
                "SET r.type = $relationshipName";
        
        Map<String, Object> params = new HashMap<>();
        params.put("paperId", paperId);
        params.put("conceptValue", conceptValue);
        params.put("relationshipName", relationshipName);
        
        session.run(query, params);
    }

    // Relationship creation methods
    private void createAuthorRelationship(Session session, Long paperId, String author, Map<String, String> row) {
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (a:Author {name: $author}) " +
                "MERGE (o:Source {name: $source}) " +
                "MERGE (p)-[:AUTHORED_BY]->(a) " +
                "MERGE (a)-[:BELONGS_TO]->(o) " +
                "MERGE (o)-[:AFFILIATED_WITH]->(:Institution {name: $organ})";

        Map<String, Object> params = new HashMap<>();
        params.put("paperId", paperId);
        params.put("author", author);
        params.put("organ", row.getOrDefault("Organ", ""));
        params.put("source", row.getOrDefault("Source", ""));

        session.run(query, params);
    }

    private void createKeywordRelationship(Session session, Long paperId, String keyword) {
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (k:Keyword {name: $keyword}) " +
                "MERGE (p)-[:HAS_KEYWORD]->(k)";
        session.run(query, Map.of("paperId", paperId, "keyword", keyword));
    }

    private void createFundRelationship(Session session, Long paperId, String fundStr) {
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (f:Fund {name: $name}) " +
                "MERGE (f)-[:FUNDED_BY]->(p)";
        session.run(query, Map.of("paperId", paperId, "name", fundStr.trim()));
    }

    private void createClcRelationship(Session session, Long paperId, String clc) {
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (c:Category {code: $code}) " +
                "MERGE (p)-[:CLASSIFIED_AS]->(c)";
        session.run(query, Map.of("paperId", paperId, "code", clc));
    }

    private void createSrcDatabaseRelationship(Session session, Long paperId, String srcDatabase) {
        String query = "MATCH (p:Paper) WHERE id(p) = $paperId " +
                "MERGE (s:SourceDB {name: $srcDatabase}) " +
                "MERGE (p)-[:FROM_SOURCE]->(s)";
        session.run(query, Map.of("paperId", paperId, "srcDatabase", srcDatabase));
    }

    public static void runNeo4jLoader(boolean ifDeleteAllNodeFirst,String title) {
        System.out.println("=== Starting Neo4jLoader ===");
        System.out.println("  ifDeleteAllNodeFirst: " + ifDeleteAllNodeFirst);
        System.out.println("  title: " + title);
        
        Neo4jLoader loader = new Neo4jLoader(Config.NEO4J_LINK, Config.NEO4J_USERNAME, Config.NEO4J_PASSWORD );
        if (ifDeleteAllNodeFirst) {
            try (Session session = driver.session()) {
                String query = "MATCH (n) DETACH DELETE n";
                session.run(query);
                LogUtil_Neo4jLoader.log("All nodes and relationships deleted");
            } catch (Exception e) {
                LogUtil_Neo4jLoader.log("Failed to delete nodes: " + e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Calling loadDataFromMySQL...");
            loader.loadDataFromMySQL(Config.MYSQL_LINK, Config.MYSQL_USERNAME, Config.MYSQL_PASSWORD,title,ifDeleteAllNodeFirst);
            LogUtil_Neo4jLoader.log("Data import completed");
            System.out.println("=== Neo4jLoader completed ===");
        } catch (Exception e) {
            LogUtil_Neo4jLoader.log("Data import failed: " + e.getMessage());
            System.err.println("Neo4j data import failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loader.close();
        }
    }


}