package com.viana.agentic;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String INPUT_ENRICHED = "account.enriched.v1";
    public static final String OUT_DECISION  = "agent.decision.v1";
    public static final String OUT_RESULT    = "agent.action_result.v1";
}
