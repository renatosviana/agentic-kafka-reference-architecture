package com.viana.agentic.memory;

import java.time.Instant;

public record MemoryHit(
        long id,
        String accountId,
        String eventId,
        String content,
        Instant createdAt,
        double distance
) {}
