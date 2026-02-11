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
        
        System.out.println("Calling Ollama API: " + OLLAMA_BASE_URL + "/api/chat");
        System.out.println("Model: " + OLLAMA_MODEL);
        
        try {
            System.out.println("Sending HTTP POST request...");
            HttpResponse<String> response = Unirest.post(OLLAMA_BASE_URL + "/api/chat")
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .asString();
            
            System.out.println("HTTP response received, status: " + response.getStatus());
            
            if (response.getStatus() != 200) {
                String errorMsg = "Ollama API error: HTTP " + response.getStatus();
                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    errorMsg += " - " + response.getBody();
                }
                throw new Exception(errorMsg);
            }
            
            System.out.println("Ollama response: " + response.getStatus() + " OK");
            System.out.println("Response body length: " + (response.getBody() != null ? response.getBody().length() : "null"));
            
            String result = parseResponse(response.getBody());
            System.out.println("Parsed content length: " + (result != null ? result.length() : "null"));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Ollama API call failed: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("  1. Is Ollama running: docker ps | grep ollama");
            System.err.println("  2. Is model downloaded: docker exec lm_ollama ollama list");
            System.err.println("  3. Is URL correct: " + OLLAMA_BASE_URL);
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Parse Ollama response and extract content
     */
    private static String parseResponse(String jsonResponse) {
        try {
            System.out.println("Parsing Ollama response...");
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            
            if (!jsonObject.has("message")) {
                System.err.println("Response missing 'message' field");
                throw new Exception("Response missing 'message' field");
            }
            
            JsonObject message = jsonObject.getAsJsonObject("message");
            if (!message.has("content")) {
                System.err.println("Message missing 'content' field");
                throw new Exception("Message missing 'content' field");
            }
            
            String content = message.get("content").getAsString();
            System.out.println("Content extracted successfully, length: " + content.length());
            
            return content;
            
        } catch (Exception e) {
            System.err.println("Failed to parse Ollama response: " + e.getMessage());
            System.err.println("Raw response length: " + (jsonResponse != null ? jsonResponse.length() : "null"));
            if (jsonResponse != null && jsonResponse.length() < 1000) {
                System.err.println("Raw response: " + jsonResponse);
            } else {
                System.err.println("Raw response (first 500 chars): " + (jsonResponse != null ? jsonResponse.substring(0, Math.min(500, jsonResponse.length())) : "null"));
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
