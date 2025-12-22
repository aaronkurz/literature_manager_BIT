package com.example.utils.result2mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.utils.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperSummarySaver {
    private static final String DB_URL = Config.MYSQL_LINK;
    private static final String USER = Config.MYSQL_USERNAME;
    private static final String PASS = Config.MYSQL_PASSWORD;

    public static void saveSummary(String model,String title,String input,String ifteacher) {
        Connection conn = null;
        try {
            // 提取有效JSON部分
            String jsonString = extractJson(input);
            if (jsonString == null) {
                throw new IllegalArgumentException("Invalid input - no JSON found");
            }

            // 解析JSON成Map以便处理字段可能为字符串或数组/对象的情况
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map<String,Object> data = mapper.readValue(jsonString, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String,Object>>(){});

            // 数据库连接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 准备SQL语句
            String sql = "INSERT INTO article_summary (" +
                    "model, title, summary,short1, short2, short3, short4, short5, short6, " +
                    "mid1, mid2, mid3, mid4, mid5, mid6, long1, long2, long3, long4, " +
                    "long5, long6, target, algmid1, algmid2, algmid3, algmid4, alglong1, " +
                    "alglong2, alglong3, alglong4, environment, tools, datas, standard, " +
                    "result, future, weekpoint,keyword,ifteacher) VALUES (" +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

            PreparedStatement pstmt = conn.prepareStatement(sql);

            // 设置参数（使用 helper 将可能为数组/对象的字段转换为字符串）
            int index = 1;
            pstmt.setString(index++, model);
            pstmt.setString(index++, title);
            pstmt.setString(index++, getStringValue(data, "summary", mapper));
            pstmt.setString(index++, getStringValue(data, "short1", mapper));
            pstmt.setString(index++, getStringValue(data, "short2", mapper));
            pstmt.setString(index++, getStringValue(data, "short3", mapper));
            pstmt.setString(index++, getStringValue(data, "short4", mapper));
            pstmt.setString(index++, getStringValue(data, "short5", mapper));
            pstmt.setString(index++, getStringValue(data, "short6", mapper));
            pstmt.setString(index++, getStringValue(data, "mid1", mapper));
            pstmt.setString(index++, getStringValue(data, "mid2", mapper));
            pstmt.setString(index++, getStringValue(data, "mid3", mapper));
            pstmt.setString(index++, getStringValue(data, "mid4", mapper));
            pstmt.setString(index++, getStringValue(data, "mid5", mapper));
            pstmt.setString(index++, getStringValue(data, "mid6", mapper));
            pstmt.setString(index++, getStringValue(data, "long1", mapper));
            pstmt.setString(index++, getStringValue(data, "long2", mapper));
            pstmt.setString(index++, getStringValue(data, "long3", mapper));
            pstmt.setString(index++, getStringValue(data, "long4", mapper));
            pstmt.setString(index++, getStringValue(data, "long5", mapper));
            pstmt.setString(index++, getStringValue(data, "long6", mapper));
            pstmt.setString(index++, getStringValue(data, "target", mapper));
            pstmt.setString(index++, getStringValue(data, "algmid1", mapper));
            pstmt.setString(index++, getStringValue(data, "algmid2", mapper));
            pstmt.setString(index++, getStringValue(data, "algmid3", mapper));
            pstmt.setString(index++, getStringValue(data, "algmid4", mapper));
            pstmt.setString(index++, getStringValue(data, "alglong1", mapper));
            pstmt.setString(index++, getStringValue(data, "alglong2", mapper));
            pstmt.setString(index++, getStringValue(data, "alglong3", mapper));
            pstmt.setString(index++, getStringValue(data, "alglong4", mapper));
            pstmt.setString(index++, getStringValue(data, "environment", mapper));
            pstmt.setString(index++, getStringValue(data, "tools", mapper));
            pstmt.setString(index++, getStringValue(data, "datas", mapper));
            pstmt.setString(index++, getStringValue(data, "standard", mapper));
            pstmt.setString(index++, getStringValue(data, "result", mapper));
            pstmt.setString(index++, getStringValue(data, "future", mapper));
            pstmt.setString(index++, getStringValue(data, "weekpoint", mapper));
            pstmt.setString(index++, getStringValue(data, "keyword", mapper));
            pstmt.setString(index++, ifteacher);

            // 执行插入
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
        // 使用正则匹配最外层{}
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    // helper 将可能为数组/对象的字段转换为字符串
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