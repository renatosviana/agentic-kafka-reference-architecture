// src/main/java/com/viana/poc/controller/AccountSummaryController.java
package com.viana.poc.controller;

import com.viana.poc.entity.AccountSummaryEntity;
import com.viana.poc.repository.AccountSummaryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/summaries")
@CrossOrigin(origins = "http://localhost:5174")
public class AccountSummaryController {

    private final AccountSummaryRepository repo;

    public AccountSummaryController(AccountSummaryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{accountId}")
    public List<AccountSummaryEntity> getSummaries(@PathVariable String accountId) {
        return repo.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
