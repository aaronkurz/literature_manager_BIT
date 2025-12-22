package com.example.utils.bigmodel;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.example.utils.Config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class QwenChat {

    static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class RequestBody {
        String model;
        Message[] messages;

        public RequestBody(String model, Message[] messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    public static String QwenChat(String model,String text) {
        try {
            // 创建请求体
            RequestBody requestBody = new RequestBody(
                    model,
                    new Message[] {
                            new Message("system", "You are a helpful assistant."),
                            new Message("user", text)
                    }
            );

            // 将请求体转换为 JSON
            String jsonInputString = new Gson().toJson(requestBody);

            // 创建 URL 对象并打开连接
            HttpURLConnection connection = (HttpURLConnection) new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Authorization", "Bearer " + Config.QWEN_API_KEY);
            connection.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
            }

            // 读取响应体
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                String responseBody = scanner.useDelimiter("\\A").next();
                return extractContentFromResponse(responseBody);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "调用失败";
    }

    // 辅助方法：从响应体中提取 content 字段
    private static String extractContentFromResponse(String responseBody) {
        try {
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = jsonObject.getAsJsonArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                if (message != null) {
                    return message.get("content").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}