package com.viana.agentic.memory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class EmbeddingsClient {

    private final RestClient restClient;

    public EmbeddingsClient(@Value("${agentic.embeddings.baseUrl}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<Double> embedOne(String text) {
        EmbeddingRequest req = new EmbeddingRequest(List.of(text));
        EmbeddingResponse resp = restClient.post()
                .uri("/embed")
                .body(req)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (resp == null || resp.vectors() == null || resp.vectors().isEmpty()) {
            throw new IllegalStateException("Embedding service returned empty vectors");
        }
        return resp.vectors().get(0);
    }
}
