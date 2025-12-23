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
        LogUtil_Neo4jLoader.log("Neo4j 连接已建立: " + uri);
    }

    public void close() {
        driver.close();
        LogUtil_Neo4jLoader.log("Neo4j 连接已关闭");
    }

    public void loadDataFromMySQL(String jdbcUrl, String jdbcUser, String jdbcPassword, String title, boolean ifDeleteAllNodeFirst) {
        LogUtil_Neo4jLoader.log("开始从 MySQL 加载数据: " + jdbcUrl);
        try (Connection mysqlConnection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
            LogUtil_Neo4jLoader.log("MySQL 连接已建立: " + jdbcUrl);

            // 动态构建查询语句
            String query = ifDeleteAllNodeFirst
                    ? "SELECT * FROM article_info"
                    : "SELECT * FROM article_info WHERE Title = ?";

            try (PreparedStatement statement = mysqlConnection.prepareStatement(query)) {
                // 当需要条件查询时设置参数
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
                    LogUtil_Neo4jLoader.log("MySQL 表结构: " + cleanedHeaders);

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
                                LogUtil_Neo4jLoader.log("检测到已有数据，停止导入");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LogUtil_Neo4jLoader.log("MySQL 连接或查询失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processRecord(Session session, Map<String, String> row) {
            LogUtil_Neo4jLoader.log("Row keys: " + row.keySet());
            String title = row.getOrDefault("Title", "").trim();
            LogUtil_Neo4jLoader.log("Resolved title: '" + title + "'");
        if (title.isEmpty()) {
            LogUtil_Neo4jLoader.log("记录无标题，跳过");
            return true;
        }

        // 提取并处理作者列表
        List<String> authors = Arrays.stream(row.getOrDefault("Author", "").split(";"))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .sorted()
                .collect(Collectors.toList());

        // 检查是否存在相同标题和作者列表的论文
        boolean exists = checkPaperExists(session, title, authors);
        if (exists) {
            LogUtil_Neo4jLoader.log("检测到重复论文: 标题='" + title + "', 作者=" + authors);
            return false;
        }

        // 创建新论文节点
        Long paperId = createPaper(session, row);
        LogUtil_Neo4jLoader.log("创建论文节点成功: ID=" + paperId + ", 标题=" + title);

        // 处理作者关系
        String[] authorArray = row.getOrDefault("Author", "").split(";");
        for (String author : authorArray) {
            author = author.trim();
            if (!author.isEmpty()) {
                createAuthorRelationship(session, paperId, author, row);
                LogUtil_Neo4jLoader.log("创建作者关系: 论文ID=" + paperId + ", 作者=" + author);
            }
        }

        // 处理其他关系（关键词、基金等）
        processRelationships(session, paperId, row);

        return true;
    }

    private boolean checkPaperExists(Session session, String title, List<String> authorList) {
        String query = "MATCH (p:论文 {title: $title})-[:作者]->(a:作者) " +
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
        String query = "CREATE (p:论文 {title: $title, doi: $doi, abstract: $abstract, " +
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
        // 处理关键词
        String[] keywords = row.getOrDefault("Keyword", "").split(";");
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (!keyword.isEmpty()) {
                createKeywordRelationship(session, paperId, keyword);
                LogUtil_Neo4jLoader.log("创建关键词关系: 论文ID=" + paperId + ", 关键词=" + keyword);
            }
        }

        // 处理基金
        String[] funds = row.getOrDefault("Fund", "").split(";");
        for (String fund : funds) {
            fund = fund.trim();
            if (!fund.isEmpty()) {
                createFundRelationship(session, paperId, fund);
                LogUtil_Neo4jLoader.log("创建基金关系: 论文ID=" + paperId + ", 基金=" + fund);
            }
        }

        // 处理分类号
        String[] clcs = row.getOrDefault("CLC", "").split(";");
        for (String clc : clcs) {
            clc = clc.trim();
            if (!clc.isEmpty()) {
                createClcRelationship(session, paperId, clc);
                LogUtil_Neo4jLoader.log("创建分类号关系: 论文ID=" + paperId + ", 分类号=" + clc);
            }
        }

        // 处理来源库
        String srcDatabase = row.get("SrcDatabase");
        if (srcDatabase != null && !srcDatabase.trim().isEmpty()) {
            createSrcDatabaseRelationship(session, paperId, srcDatabase.trim());
            LogUtil_Neo4jLoader.log("创建来源库关系: 论文ID=" + paperId + ", 来源库=" + srcDatabase);
        }
        
        // 处理自定义概念
        processCustomConcepts(session, paperId, row);
    }
    
    /**
     * Process custom concepts and create relationships
     */
    private void processCustomConcepts(Session session, Long paperId, Map<String, String> row) {
        for (int i = 1; i <= 3; i++) {
            String customConceptKey = "custom_concept" + i;
            String customConceptJson = row.get(customConceptKey);
            
            if (customConceptJson == null || customConceptJson.trim().isEmpty()) {
                continue;
            }
            
            try {
                // Parse JSON: {"relationshipName": "method", "matchingConcepts": ["RCT", "Cohort"]}
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(customConceptJson).getAsJsonObject();
                
                if (!json.has("relationshipName") || !json.has("matchingConcepts")) {
                    continue;
                }
                
                String relationshipName = json.get("relationshipName").getAsString();
                com.google.gson.JsonArray concepts = json.getAsJsonArray("matchingConcepts");
                
                if (concepts.size() == 0) {
                    continue;
                }
                
                // Create relationship for each matching concept
                for (int j = 0; j < concepts.size(); j++) {
                    String conceptValue = concepts.get(j).getAsString();
                    createCustomConceptRelationship(session, paperId, relationshipName, conceptValue);
                    LogUtil_Neo4jLoader.log("创建自定义概念关系: 论文ID=" + paperId + 
                        ", 关系=" + relationshipName + ", 概念=" + conceptValue);
                }
                
            } catch (Exception e) {
                LogUtil_Neo4jLoader.log("解析自定义概念失败: " + customConceptKey + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Create custom concept relationship
     */
    private void createCustomConceptRelationship(Session session, Long paperId, String relationshipName, String conceptValue) {
        // Using dynamic relationship type is not directly supported in Cypher, 
        // so we'll create a generic CUSTOM_CONCEPT relationship with properties
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (c:自定义概念 {name: $conceptValue, relationship: $relationshipName}) " +
                "MERGE (p)-[r:自定义概念关系]->(c) " +
                "SET r.type = $relationshipName";
        
        Map<String, Object> params = new HashMap<>();
        params.put("paperId", paperId);
        params.put("conceptValue", conceptValue);
        params.put("relationshipName", relationshipName);
        
        session.run(query, params);
    }

    // 以下关系创建方法保持不变
    private void createAuthorRelationship(Session session, Long paperId, String author, Map<String, String> row) {
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (a:作者 {name: $author}) " +
                "MERGE (o:文献来源 {name: $source}) " +
                "MERGE (p)-[:作者]->(a) " +
                "MERGE (a)-[:所属]->(o) " +
                "MERGE (o)-[:隶属于]->(:单位 {name: $organ})";

        Map<String, Object> params = new HashMap<>();
        params.put("paperId", paperId);
        params.put("author", author);
        params.put("organ", row.getOrDefault("Organ", ""));
        params.put("source", row.getOrDefault("Source", ""));

        session.run(query, params);
    }

    private void createKeywordRelationship(Session session, Long paperId, String keyword) {
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (k:关键词 {name: $keyword}) " +
                "MERGE (p)-[:关键词]->(k)";
        session.run(query, Map.of("paperId", paperId, "keyword", keyword));
    }

    private void createFundRelationship(Session session, Long paperId, String fundStr) {
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (f:基金 {name: $name}) " +
                "MERGE (f)-[:资助]->(p)";
        session.run(query, Map.of("paperId", paperId, "name", fundStr.trim()));
    }

    private void createClcRelationship(Session session, Long paperId, String clc) {
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (c:分类号 {code: $code}) " +
                "MERGE (p)-[:分类]->(c)";
        session.run(query, Map.of("paperId", paperId, "code", clc));
    }

    private void createSrcDatabaseRelationship(Session session, Long paperId, String srcDatabase) {
        String query = "MATCH (p:论文) WHERE id(p) = $paperId " +
                "MERGE (s:来源库 {name: $srcDatabase}) " +
                "MERGE (p)-[:来源库]->(s)";
        session.run(query, Map.of("paperId", paperId, "srcDatabase", srcDatabase));
    }

    public static void runNeo4jLoader(boolean ifDeleteAllNodeFirst,String title) {
        Neo4jLoader loader = new Neo4jLoader(Config.NEO4J_LINK, Config.NEO4J_USERNAME, Config.NEO4J_PASSWORD );
        if (ifDeleteAllNodeFirst) {
            try (Session session = driver.session()) {
                String query = "MATCH (n) DETACH DELETE n";
                session.run(query);
                LogUtil_Neo4jLoader.log("已删除所有节点及其关系");
            } catch (Exception e) {
                LogUtil_Neo4jLoader.log("删除节点失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            loader.loadDataFromMySQL(Config.MYSQL_LINK, Config.MYSQL_USERNAME, Config.MYSQL_PASSWORD,title,ifDeleteAllNodeFirst);
            LogUtil_Neo4jLoader.log("数据导入完成");
        } catch (Exception e) {
            LogUtil_Neo4jLoader.log("数据导入失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loader.close();
        }
    }


}