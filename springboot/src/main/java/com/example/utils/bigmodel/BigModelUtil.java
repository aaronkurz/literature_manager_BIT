package com.example.utils.bigmodel;

import com.example.utils.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM utility - uses local Ollama with Ministral 3B
 */
public class BigModelUtil {
    // Ollama configuration
    private static final String OLLAMA_BASE_URL = Config.OLLAMA_BASE_URL;
    private static final String OLLAMA_MODEL = Config.OLLAMA_MODEL;

    private static final Gson gson = new Gson();

    // Ollama text generation
    public static String ollamaTextGeneration(String content) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", "You are a helpful assistant."));
        messages.add(createMessage("user", content));

        return sendRequest(OLLAMA_BASE_URL, OLLAMA_MODEL, messages);
    }

    // Ollama document understanding (uses same API)
    public static String ollamaDocumentUnderstanding(String content, String filePath) throws Exception {
        // For now, we'll just use text generation
        // In the future, we could read the file and pass its content
        return ollamaTextGeneration(content);
    }

    private static Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private static String sendRequest(String baseUrl, String model,
                                      List<Map<String, String>> messages)
            throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.add("model", gson.toJsonTree(model));
        requestBody.add("messages", gson.toJsonTree(messages));
        requestBody.addProperty("stream", false);

        HttpResponse<String> response = Unirest.post(baseUrl + "/api/chat")
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .asString();

        return parseResponse(response.getBody());
    }

    private static String parseResponse(String jsonResponse) {
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        return jsonObject.getAsJsonObject("message")
                .get("content").getAsString();
    }

    public static String getOllamaBaseUrl() {
        return OLLAMA_BASE_URL;
    }

    public static String getOllamaModel() {
        return OLLAMA_MODEL;
    }
}