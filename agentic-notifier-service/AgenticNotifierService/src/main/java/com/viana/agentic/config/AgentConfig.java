package com.viana.agentic.config;

import com.viana.agentic.decision.DecisionEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {
  @Bean
  public DecisionEngine decisionEngine() {
    return new DecisionEngine();
  }
}
