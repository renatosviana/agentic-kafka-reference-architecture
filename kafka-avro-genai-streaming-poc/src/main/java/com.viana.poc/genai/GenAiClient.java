package com.viana.poc.genai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GenAiClient {

    private static final Logger log = LoggerFactory.getLogger(GenAiClient.class);

    private final WebClient genAiWebClient;

    public GenAiClient(WebClient genAiWebClient) {
        this.genAiWebClient = genAiWebClient;
    }

    public GenAiResponse summarizeEvent(GenAiRequest request) {

        String prompt = """
                You are an AI assistant analyzing banking account events.

                Your task:
                1. Summarize the event in 1–2 sentences.
                2. Identify whether it is a CREDIT or DEBIT transaction.
                3. Mention any anomaly or unusual behavior if applicable.

                Event:
                - accountId: %s
                - eventType: %s
                - amount: %.2f
                - newBalance: %.2f

                Respond with a concise summary in natural language.
                """
                .formatted(
                        request.getAccountId(),
                        request.getEventType(),
                        request.getAmount(),
                        request.getNewBalance()
                );

        try {
            PromptPayload payload = new PromptPayload(prompt);

            GenAiResponse response = genAiWebClient.post()
                    .uri("/summarize-event")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(GenAiResponse.class)
                    .block(); // TODO: make this reactive later

            if (response == null || response.getSummary() == null) {
                return new GenAiResponse("No summary generated.");
            }
            return response;

        } catch (Exception e) {
            log.error("Error calling GenAI service", e);
            return new GenAiResponse("GenAI service unavailable; skipping summary.");
        }
    }

    /**
     * Simple wrapper for the payload sent to the GenAI HTTP API.
     */
    private static class PromptPayload {
        private String prompt;

        public PromptPayload(String prompt) {
            this.prompt = prompt;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
