package com.viana.agentic.api;

import com.viana.agentic.executor.ActionExecutor;
import com.viana.agentic.kafka.EnrichedEventListener;
import com.viana.common.events.EnrichedAccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
//@Profile({"dev", "local", "test"}) TODO: Fix
public class AgentTestController {
    private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);

    private final EnrichedEventListener listener;

    public AgentTestController(EnrichedEventListener listener) {
        this.listener = listener;
    }

    @PostMapping("/enriched-event")
    public ResponseEntity<?> testAgent(@RequestBody EnrichedAccountEvent event) {
        listener.onMessage(event);
        log.info("Agent Summary {}", event.summary());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/ping")
    public String ping(){ return "ok"; }

}
