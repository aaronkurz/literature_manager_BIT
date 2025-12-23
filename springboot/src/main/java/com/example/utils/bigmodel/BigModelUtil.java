package com.example.utils.bigmodel;

import com.example.utils.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM utility - uses local Ollama with Ministral-3:3b
 * Optimized for speed and accuracy on end-user hardware
 */
public class BigModelUtil {
    private static final String OLLAMA_BASE_URL = Config.OLLAMA_BASE_URL;
    private static final String OLLAMA_MODEL = Config.OLLAMA_MODEL;
    
    // Reduced timeout for faster failure detection (3 minutes max)
    private static final int SOCKET_TIMEOUT_MS = (int) Duration.ofMinutes(3).toMillis();
    private static final int CONNECT_TIMEOUT_MS = (int) Duration.ofSeconds(15).toMillis();
    
    static {
        Unirest.setTimeouts(CONNECT_TIMEOUT_MS, SOCKET_TIMEOUT_MS);
    }
    
    private static final Gson gson = new Gson();
    
    /**
     * Generate text using Ollama
     */
    public static String ollamaTextGeneration(String content) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System message for JSON output
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", 
            "You are a helpful assistant that always responds with valid JSON. " +
            "Never use markdown code blocks (```json). " +
            "Always return a single JSON object, not an array. " +
            "Use empty strings \"\" for unknown values.");
        messages.add(systemMsg);
        
        // User message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", content);
        messages.add(userMsg);
        
        return sendRequest(messages);
    }
    
    /**
     * Send request to Ollama API
     */
    private static String sendRequest(List<Map<String, String>> messages) throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.add("model", gson.toJsonTree(OLLAMA_MODEL));
        requestBody.add("messages", gson.toJsonTree(messages));
        requestBody.addProperty("stream", false);
        
        // Performance options for 3B model
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.7);
        options.addProperty("top_p", 0.9);
        options.addProperty("num_predict", 2048); // Max tokens to generate
        requestBody.add("options", options);
        
        System.out.println("调用Ollama API: " + OLLAMA_BASE_URL + "/api/chat");
        System.out.println("模型: " + OLLAMA_MODEL);
        
        try {
            System.out.println("发送 HTTP POST 请求...");
            HttpResponse<String> response = Unirest.post(OLLAMA_BASE_URL + "/api/chat")
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .asString();
            
            System.out.println("收到 HTTP 响应，状态码: " + response.getStatus());
            
            if (response.getStatus() != 200) {
                String errorMsg = "Ollama API错误: HTTP " + response.getStatus();
                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    errorMsg += " - " + response.getBody();
                }
                throw new Exception(errorMsg);
            }
            
            System.out.println("Ollama响应: " + response.getStatus() + " OK");
            System.out.println("响应体长度: " + (response.getBody() != null ? response.getBody().length() : "null"));
            
            String result = parseResponse(response.getBody());
            System.out.println("解析后内容长度: " + (result != null ? result.length() : "null"));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Ollama API调用失败: " + e.getMessage());
            System.err.println("请检查:");
            System.err.println("  1. Ollama服务是否运行: docker ps | grep ollama");
            System.err.println("  2. 模型是否已下载: docker exec lm_ollama ollama list");
            System.err.println("  3. URL是否正确: " + OLLAMA_BASE_URL);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Parse Ollama response and extract content
     */
    private static String parseResponse(String jsonResponse) {
        try {
            System.out.println("开始解析 Ollama 响应...");
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            
            if (!jsonObject.has("message")) {
                System.err.println("响应中缺少'message'字段");
                throw new Exception("响应中缺少'message'字段");
            }
            
            JsonObject message = jsonObject.getAsJsonObject("message");
            if (!message.has("content")) {
                System.err.println("消息中缺少'content'字段");
                throw new Exception("消息中缺少'content'字段");
            }
            
            String content = message.get("content").getAsString();
            System.out.println("成功提取内容，长度: " + content.length());
            
            return content;
            
        } catch (Exception e) {
            System.err.println("解析Ollama响应失败: " + e.getMessage());
            System.err.println("原始响应长度: " + (jsonResponse != null ? jsonResponse.length() : "null"));
            if (jsonResponse != null && jsonResponse.length() < 1000) {
                System.err.println("原始响应: " + jsonResponse);
            } else {
                System.err.println("原始响应（前500字符）: " + (jsonResponse != null ? jsonResponse.substring(0, Math.min(500, jsonResponse.length())) : "null"));
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public static String getOllamaBaseUrl() {
        return OLLAMA_BASE_URL;
    }
    
    public static String getOllamaModel() {
        return OLLAMA_MODEL;
    }
}
