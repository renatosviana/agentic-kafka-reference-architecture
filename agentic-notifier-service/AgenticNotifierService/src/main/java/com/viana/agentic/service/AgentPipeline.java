package com.viana.agentic.service;

import com.viana.agentic.decision.DecisionEngine;
import com.viana.agentic.executor.ActionExecutor;
import com.viana.agentic.kafka.AuditPublisher;
import com.viana.agentic.model.AgentDecision;
import com.viana.agentic.model.EnrichedAccountEvent;
import org.springframework.stereotype.Service;

@Service
public class AgentPipeline {

    private final DecisionEngine decisionEngine;
    private final ActionExecutor actionExecutor;
    private final AuditPublisher auditPublisher;

    public AgentPipeline(DecisionEngine decisionEngine,
                         ActionExecutor actionExecutor,
                         AuditPublisher auditPublisher) {
        this.decisionEngine = decisionEngine;
        this.actionExecutor = actionExecutor;
        this.auditPublisher = auditPublisher;
    }

    public AgentDecision handle(EnrichedAccountEvent event) {
        AgentDecision decision = decisionEngine.decide(event);

        decision.actions().forEach(action ->
                actionExecutor.execute(decision, event, action)
        );

        auditPublisher.publishDecision(event, decision);

        return decision;
    }

}
