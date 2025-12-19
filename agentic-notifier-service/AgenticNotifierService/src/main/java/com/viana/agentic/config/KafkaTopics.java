package com.viana.agentic.config;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String INPUT_ENRICHED = "account.enriched.events.v1";

    public static final String OUT_DECISION = "agent.decision.events.v1";
    public static final String OUT_RESULT   = "agent.action.result.v1";

}
