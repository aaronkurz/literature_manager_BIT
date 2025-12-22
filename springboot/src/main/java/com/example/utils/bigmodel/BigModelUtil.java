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
 * LLM utility - uses local Ollama with Mistral 3B
 */
public class BigModelUtil {
    // Ollama configuration
    private static final String OLLAMA_BASE_URL = Config.OLLAMA_BASE_URL;
    private static final String OLLAMA_MODEL = Config.OLLAMA_MODEL;

    private static final Gson gson = new Gson();

    // Ollama text generation with context management
    public static String ollamaTextGeneration(String content) throws Exception {
        // Ministral-3 has 256K context window, but for efficiency on 3B model,
        // we still limit to avoid very long processing times on end-user hardware
        int maxChars = 32000; // ~8000 tokens - conservative for 3B model
        if (content.length() > maxChars) {
            System.out.println("警告: 文本过长(" + content.length() + "字符), 截取前" + maxChars + "字符处理");
            content = content.substring(0, maxChars) + "\n...(内容已截断)";
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", "You are a helpful assistant specializing in academic paper analysis. Always respond in JSON format."));
        messages.add(createMessage("user", content));

        return sendRequest(OLLAMA_BASE_URL, OLLAMA_MODEL, messages);
    }

    // Extract metadata from PDF text
    public static String extractMetadata(String pdfText) throws Exception {
        // Truncate to first ~8000 chars which usually contains title, abstract, authors
        int maxChars = 8000;
        String relevantText = pdfText.length() > maxChars ? pdfText.substring(0, maxChars) : pdfText;
        
        String prompt = "从以下学术论文文本中提取元数据。请严格按照JSON格式返回:\n\n" + 
                       Config.METADATA_EXTRACTION_JSON + 
                       "\n\n论文文本:\n" + relevantText +
                       "\n\n请仅返回JSON，不要有其他说明文字。";
        
        return ollamaTextGeneration(prompt);
    }

    // Ollama document understanding (uses same API)
    public static String ollamaDocumentUnderstanding(String content, String filePath) throws Exception {
        // For now, we'll just use text generation
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

        System.out.println("正在调用Ollama API: " + baseUrl + "/api/chat");
        System.out.println("使用模型: " + model);
        
        try {
            HttpResponse<String> response = Unirest.post(baseUrl + "/api/chat")
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .asString();

            if (response.getStatus() != 200) {
                throw new Exception("Ollama API错误: HTTP " + response.getStatus() + " - " + response.getBody());
            }

            System.out.println("Ollama响应状态: " + response.getStatus());
            return parseResponse(response.getBody());
        } catch (Exception e) {
            System.err.println("Ollama API调用失败: " + e.getMessage());
            System.err.println("请确保:");
            System.err.println("1. Ollama服务正在运行");
            System.err.println("2. 模型已下载: docker exec lm_ollama ollama pull " + model);
            System.err.println("3. URL正确: " + baseUrl);
            throw e;
        }
    }

    private static String parseResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            if (jsonObject.has("message")) {
                return jsonObject.getAsJsonObject("message")
                        .get("content").getAsString();
            } else {
                throw new Exception("响应中没有'message'字段: " + jsonResponse);
            }
        } catch (Exception e) {
            System.err.println("解析Ollama响应失败: " + e.getMessage());
            System.err.println("原始响应: " + jsonResponse);
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