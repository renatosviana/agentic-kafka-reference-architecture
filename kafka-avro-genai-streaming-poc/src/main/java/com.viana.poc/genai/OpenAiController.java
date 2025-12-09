package com.viana.poc.genai;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viana.poc.service.AccountEventConsumer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class OpenAiController {
    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final OkHttpClient httpClient = new OkHttpClient();

    private final String apiKey;
    private final String model;

    public OpenAiController(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * POST /summarize-event
     * Body: { "prompt": "..." }
     * Response: { "summary": "..." }
     */
    @PostMapping("/summarize-event")
    public GenAiResponse summarize(@RequestBody PromptPayload payload) {
        try {
            ChatRequest requestBody = new ChatRequest(
                    model,
                    List.of(new Message("user", payload.prompt()))
            );

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(okhttp3.RequestBody.create(
                            jsonRequest,
                            MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    String errBody = response.body() != null ? response.body().string() : "<no body>";
                    throw new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "OpenAI error: " + response.code() + " " + response.message() + " - " + errBody
                    );
                }

                String jsonResponse = response.body().string();
                ChatResponse chatResponse =
                        objectMapper.readValue(jsonResponse, ChatResponse.class);

                String content = chatResponse
                        .choices()
                        .get(0)
                        .message()
                        .content();

                return new GenAiResponse(content);
            }
        } catch (IOException e) {
            log.error("Error calling OpenAI: {}", e, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to talk to OpenAI: " + e.getMessage(), e
            );
        }
    }

    // DTOs

    public record PromptPayload(String prompt) {}

    public record GenAiResponse(String summary) {}

    public record ChatRequest(
            String model,
            List<Message> messages
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String role, String content) {}

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
