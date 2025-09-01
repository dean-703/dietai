package com.courtney.dietai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class OpenAIService {
    private String apiKey;
    private String model = "gpt-4o-mini";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String analyzeDiet(String jsonSummary) throws Exception {
        Objects.requireNonNull(jsonSummary, "jsonSummary cannot be null");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key not set.");
        }

        String systemPrompt =
                "You are a registered dietitian analyzing nutrition tracking data with user profile and targets. " +
                        "Given a JSON summary including profile, goals, calculated targets, and actual averages, " +
                        "produce a concise, friendly, practical assessment in 250-400 words. " +
                        "Use short bullet points followed by a brief action plan. Avoid medical advice. " +
                        "Cover: energy balance vs target, macro alignment (protein/carbs/fat), fiber and sodium vs targets, " +
                        "consistency/meal patterns, and 3-5 specific food or habit swaps tailored to the user's context.";

        String userPrompt = "Analyze this dataset and personalize the feedback:\n" + jsonSummary;

        String payload = """
                {
                  "model": "%s",
                  "temperature": 0.4,
                  "max_tokens": 700,
                  "messages": [
                    {"role": "system", "content": %s},
                    {"role": "user", "content": %s}
                  ]
                }
                """.formatted(escapeJson(model), quote(systemPrompt), quote(userPrompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new RuntimeException("OpenAI HTTP " + response.statusCode() + ": " + response.body());
        }
        String body = response.body();
        JsonNode root = mapper.readTree(body);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new RuntimeException("Unexpected OpenAI response: " + body);
        }
        String content = choices.get(0).path("message").path("content").asText(null);
        if (content == null) throw new RuntimeException("No content in OpenAI response.");
        return content.trim();
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) {
        if (model != null && !model.isBlank()) this.model = model.trim();
    }
}