package com.viana.agentic.model;

import java.time.Instant;
import java.util.List;

public record AgentDecision(
        String decisionId,
        String eventId,
        String accountId,
        Instant createdAt,
        List<AgentAction> actions,
        String rationale
) {}
