package com.viana.agentic.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record AgentDecision(
        String decisionId,
        String eventId,
        String accountId,
        Instant createdAt,
        List<AgentAction> actions,
        String rationale
) {}
