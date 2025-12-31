package com.viana.agentic.memory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryService {

    private final boolean enabled;
    private final int topK;
    private final EmbeddingsClient embeddings;
    private final VectorStore store;

    public MemoryService(
            @Value("${agentic.memory.enabled:true}") boolean enabled,
            @Value("${agentic.memory.topK:5}") int topK,
            EmbeddingsClient embeddings,
            VectorStore store
    ) {
        this.enabled = enabled;
        this.topK = topK;
        this.embeddings = embeddings;
        this.store = store;
    }

    public void remember(String accountId, String eventId, String content) {
        if (!enabled) return;
        List<Double> vec = embeddings.embedOne(content);
        store.insertMemory(accountId, eventId, content, vec);
    }

    public List<MemoryHit> recallSimilar(String accountId, String excludeEventId, String queryText) {
        if (!enabled) return List.of();
        List<Double> q = embeddings.embedOne(queryText);
        return store.searchSimilar(accountId, excludeEventId, q, topK);
    }
}
