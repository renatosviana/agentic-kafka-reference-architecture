package com.viana.agentic.api;

import com.viana.agentic.memory.MemoryHit;
import com.viana.agentic.memory.MemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dev/memory")
public class MemoryTestController {

    private final MemoryService memoryService;

    public MemoryTestController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @PostMapping(value = "/remember", consumes = "text/plain")
    public void remember(@RequestParam String accountId,
                         @RequestParam String eventId,
                         @RequestBody String content) {
        memoryService.remember(accountId, eventId, content);
    }

    @GetMapping("/recall")
    public List<MemoryHit> recall(@RequestParam String accountId,
                                 @RequestParam(required = false) String excludeEventId,
                                 @RequestParam String query) {
        return memoryService.recallSimilar(accountId, excludeEventId, query);
    }
}
