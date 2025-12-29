package com.viana.common.events;

import lombok.Builder;

import java.time.Instant;

@Builder
public record EnrichedAccountEvent(
        String eventId,
        String accountId,
        Integer riskScore,
        String summary,
        EventType eventType,
        Double amount,
        String currency,
        Instant timestamp
) {}
