package com.example.utils.result2mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.utils.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Paper summary saver - simplified for single summary length
 */
public class PaperSummarySaver {
    private static final String DB_URL = Config.MYSQL_LINK;
    private static final String USER = Config.MYSQL_USERNAME;
    private static final String PASS = Config.MYSQL_PASSWORD;

    public static void saveSummary(String model, String title, String input, String ifteacher) {
        Connection conn = null;
        try {
            // Extract valid JSON portion
            String jsonString = extractJson(input);
            if (jsonString == null) {
                throw new IllegalArgumentException("Invalid input - no JSON found");
            }

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String,Object> data = mapper.readValue(jsonString, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String,Object>>(){});

            // Database connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Prepare SQL statement with new simplified schema
            String sql = "INSERT INTO article_summary (" +
                    "model, title, fullSummary, summary1, summary2, summary3, summary4, summary5, summary6, " +
                    "target, algorithm1, algorithm2, algorithm3, algorithm4, " +
                    "environment, tools, datas, standard, " +
                    "result, future, weekpoint, keyword, ifteacher) VALUES (" +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

            PreparedStatement pstmt = conn.prepareStatement(sql);

            // Set parameters
            int index = 1;
            pstmt.setString(index++, model);
            pstmt.setString(index++, title);
            pstmt.setString(index++, getStringValue(data, "fullSummary", mapper));
            pstmt.setString(index++, getStringValue(data, "summary1", mapper));
            pstmt.setString(index++, getStringValue(data, "summary2", mapper));
            pstmt.setString(index++, getStringValue(data, "summary3", mapper));
            pstmt.setString(index++, getStringValue(data, "summary4", mapper));
            pstmt.setString(index++, getStringValue(data, "summary5", mapper));
            pstmt.setString(index++, getStringValue(data, "summary6", mapper));
            pstmt.setString(index++, getStringValue(data, "target", mapper));
            pstmt.setString(index++, getStringValue(data, "algorithm1", mapper));
            pstmt.setString(index++, getStringValue(data, "algorithm2", mapper));
            pstmt.setString(index++, getStringValue(data, "algorithm3", mapper));
            pstmt.setString(index++, getStringValue(data, "algorithm4", mapper));
            pstmt.setString(index++, getStringValue(data, "environment", mapper));
            pstmt.setString(index++, getStringValue(data, "tools", mapper));
            pstmt.setString(index++, getStringValue(data, "datas", mapper));
            pstmt.setString(index++, getStringValue(data, "standard", mapper));
            pstmt.setString(index++, getStringValue(data, "result", mapper));
            pstmt.setString(index++, getStringValue(data, "future", mapper));
            pstmt.setString(index++, getStringValue(data, "weekpoint", mapper));
            pstmt.setString(index++, getStringValue(data, "keyword", mapper));
            pstmt.setString(index++, ifteacher);

            // Execute insert
            pstmt.executeUpdate();
            pstmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    private static String extractJson(String input) {
        // Match outermost {}
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    // Helper to convert array/object fields to string
    private static String getStringValue(java.util.Map<String,Object> data, String key, com.fasterxml.jackson.databind.ObjectMapper mapper) {
        if (data == null || !data.containsKey(key)) return null;
        Object val = data.get(key);
        if (val == null) return null;
        try {
            if (val instanceof String) return (String) val;
            if (val instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) val;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    Object e = list.get(i);
                    if (e instanceof String) sb.append((String) e);
                    else sb.append(mapper.writeValueAsString(e));
                    if (i < list.size() - 1) sb.append("\n");
                }
                return sb.toString();
            }
            if (val instanceof java.util.Map) {
                return mapper.writeValueAsString(val);
            }
            return val.toString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}