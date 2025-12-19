package com.viana.agentic.model;

import java.util.Map;

public record AgentAction(
        String type,
        Map<String, Object> args
) {}
