// src/main/java/com/viana/poc/genai/GenAiService.java
package com.viana.poc.genai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GenAiService {

    private static final Logger log = LoggerFactory.getLogger(GenAiService.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GenAiService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public GenAiResult generateSummary(String prompt) {
        // Delegate to summarize with a neutral balance
        return summarize(prompt, 0.0);
    }

    /**
     * Main API used by the Kafka consumer.
     * Builds a GenAiResult (summary + classification + riskScore),
     * with retries and a safe fallback.
     */
    public GenAiResult summarize(String prompt, double updatedBalance) {
        int maxRetries = 3;
        long backoffMillis = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String summary = callOpenAiOnce(prompt);

                String classification =
                        updatedBalance < 0 ? "NEGATIVE_BALANCE" : "NORMAL";
                int riskScore =
                        updatedBalance < 0 ? 70 : 10;

                return GenAiResult.success(summary, classification, riskScore);
            } catch (Exception ex) {
                log.warn("OpenAI call failed (attempt {}/{}): {}",
                        attempt, maxRetries, ex.getMessage());

                if (attempt == maxRetries) {
                    return GenAiResult.fallback(
                            "GenAI service is temporarily unavailable. " +
                                    "This transaction could not be automatically summarized."
                    );
                }

                try {
                    Thread.sleep(backoffMillis * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return GenAiResult.fallback(
                            "Interrupted while retrying GenAI call."
                    );
                }
            }
        }

        return GenAiResult.fallback("Unexpected error calling GenAI.");
    }

    /**
     * Single HTTP call to OpenAI Chat Completions.
     */
    private String callOpenAiOnce(String prompt) throws IOException {
        ChatRequest requestBody = new ChatRequest(
                model,
                List.of(new Message("user", prompt))
        );

        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        jsonRequest,
                        MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("OpenAI error: " + response.code() + " - " + response.message());
            }

            String jsonResponse = response.body().string();
            ChatResponse chatResponse =
                    objectMapper.readValue(jsonResponse, ChatResponse.class);

            return chatResponse
                    .choices()
                    .get(0)
                    .message()
                    .content();
        }
    }

    // ===== DTOs used only in this service =====

    public record ChatRequest(
            String model,
            List<Message> messages
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            String role,
            String content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatResponse(
            List<Choice> choices
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Choice(
                Message message
        ) {}
    }
}
