package com.viana.poc.events;

import java.time.Instant;

public record EnrichedAccountEvent(
    String eventId,
    String accountId,
    int riskScore,
    String summary,
    Instant timestamp
) {}
