package com.viana.agentic.model;

public record EnrichedAccountEvent(
        String eventId,
        String accountId,
        Integer riskScore,
        String summary
) {}
