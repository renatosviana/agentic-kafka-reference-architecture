package com.viana.agentic.model;

import java.time.Instant;

public record ActionResult(
        String decisionId,
        String eventId,
        String actionType,
        Instant executedAt,
        boolean success,
        String message
) {}
