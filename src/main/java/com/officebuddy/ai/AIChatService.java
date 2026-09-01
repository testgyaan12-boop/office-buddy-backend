package com.officebuddy.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AIChatService {

    private static final Logger log = LoggerFactory.getLogger(AIChatService.class);
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public AIChatService(
            @Value("${app.ai.gemini-api-key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String chat(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "AI assistant is not configured. Please set a Gemini API key.";
        }

        try {
            var requestBody = buildRequestBody(message);
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var body = response.body();

            if (response.statusCode() != 200) {
                log.warn("Gemini API returned {}: {}", response.statusCode(), body);
            }

            return extractResponse(body);
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return "Sorry, I'm having trouble connecting. Please try again.";
        }
    }

    private String buildRequestBody(String message) {
        try {
            var root = mapper.createObjectNode();
            var contents = root.putArray("contents");
            var content = contents.addObject();
            var parts = content.putArray("parts");
            parts.addObject().put("text", message);
            return root.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private String extractResponse(String responseBody) {
        try {
            var root = mapper.readTree(responseBody);

            // Check for Gemini error response
            var error = root.get("error");
            if (error != null) {
                var msg = error.get("message").asText("Unknown error");
                log.warn("Gemini API error: {}", msg);
                return "Error: " + msg;
            }

            // Check promptFeedback for blocked content
            var feedback = root.get("promptFeedback");
            if (feedback != null) {
                var blockReason = feedback.get("blockReason");
                if (blockReason != null) {
                    return "Content blocked: " + blockReason.asText();
                }
            }

            // Parse successful response
            var candidates = root.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                var content = candidates.get(0).get("content");
                if (content != null) {
                    var parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        var text = parts.get(0).get("text");
                        if (text != null) {
                            return text.asText();
                        }
                    }
                }
            }

            log.warn("Unexpected Gemini response structure: {}", responseBody);
            return "I couldn't process that. Please try again.";
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", responseBody, e);
            return "Sorry, something went wrong.";
        }
    }
}
