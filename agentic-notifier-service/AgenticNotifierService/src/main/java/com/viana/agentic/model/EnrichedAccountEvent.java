package com.viana.agentic.model;

import lombok.Builder;

@Builder
public record EnrichedAccountEvent(
        String eventId,
        String accountId,
        Integer riskScore,
        String summary
) {}
