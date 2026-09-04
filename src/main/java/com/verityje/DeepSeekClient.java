package com.verityje;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

final class DeepSeekClient {
    private static final String DEFAULT_URL = "https://api.deepseek.com/chat/completions";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    private DeepSeekClient(String apiKey, String apiUrl, String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    }

    static DeepSeekClient fromEnvironment() {
        return new DeepSeekClient(
            setting("DEEPSEEK_API_KEY", "deepseek.apiKey", ""),
            setting("DEEPSEEK_API_URL", "deepseek.apiUrl", DEFAULT_URL),
            setting("DEEPSEEK_MODEL", "deepseek.model", DEFAULT_MODEL));
    }

    boolean isConfigured() {
        return !apiKey.isBlank();
    }

    CompletableFuture<String> ask(String question) {
        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.addProperty("stream", false);
        body.addProperty("max_tokens", 800);

        JsonArray messages = new JsonArray();
        messages.add(message("system",
            "你是 Minecraft Java Edition 游戏内助手 Verity。请用玩家所用语言简洁回答，避免 Markdown 表格。"));
        messages.add(message("user", question));
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
            .timeout(Duration.ofSeconds(90))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> parseResponse(response.statusCode(), response.body()));
    }

    private static JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private static String parseResponse(int status, String body) {
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        if (status < 200 || status >= 300) {
            String detail = json.has("error") && json.getAsJsonObject("error").has("message")
                ? json.getAsJsonObject("error").get("message").getAsString()
                : "HTTP " + status;
            throw new IllegalStateException(detail);
        }
        return json.getAsJsonArray("choices").get(0).getAsJsonObject()
            .getAsJsonObject("message").get("content").getAsString().strip();
    }

    private static String setting(String environmentName, String propertyName, String fallback) {
        String property = System.getProperty(propertyName);
        if (property != null && !property.isBlank()) return property;
        String environment = System.getenv(environmentName);
        return environment == null || environment.isBlank() ? fallback : environment;
    }
}
