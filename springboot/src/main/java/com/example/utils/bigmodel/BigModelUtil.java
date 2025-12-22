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
 * LLM utility - simplified to use only Qwen
 */
public class BigModelUtil {
    // Qwen configuration
    private static final String QWEN_API_KEY = Config.QWEN_API_KEY;
    private static final String QWEN_BASE_URL = Config.QWEN_BASE_URL;
    private static final String QWEN_MODEL = Config.QWEN_MODEL;
    private static final String QWEN_DOC_MODEL = Config.QWEN_DOC_MODEL;

    private static final Gson gson = new Gson();

    // Qwen text generation
    public static String qwenTextGeneration(String content) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", "You are a helpful assistant."));
        messages.add(createMessage("user", content));

        return sendRequest(QWEN_BASE_URL, QWEN_API_KEY, QWEN_MODEL, messages, null);
    }

    // Qwen document understanding
    public static String qwenDocumentUnderstanding(String content, String filePath) throws Exception {
        return QwenDoc.kimidoc(filePath, content, QWEN_DOC_MODEL);
    }

    private static Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private static String sendRequest(String baseUrl, String apiKey, String model,
                                      List<Map<String, String>> messages, Float temperature)
            throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.add("model", gson.toJsonTree(model));
        requestBody.add("messages", gson.toJsonTree(messages));
        if (temperature != null) {
            requestBody.add("temperature", gson.toJsonTree(temperature));
        }

        HttpResponse<String> response = Unirest.post(baseUrl + "/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody.toString())
                .asString();

        return parseResponse(response.getBody());
    }

    private static String parseResponse(String jsonResponse) {
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        return jsonObject.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .get("message").getAsJsonObject()
                .get("content").getAsString();
    }

    public static String getQwenApiKey() {
        return QWEN_API_KEY;
    }

    public static String getQwenBaseUrl() {
        return QWEN_BASE_URL;
    }
}