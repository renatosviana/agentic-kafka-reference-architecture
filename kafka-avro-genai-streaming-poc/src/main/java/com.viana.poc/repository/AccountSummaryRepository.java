// src/main/java/com/viana/poc/repository/AccountSummaryRepository.java
package com.viana.poc.repository;

import com.viana.poc.entity.AccountSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountSummaryRepository
        extends JpaRepository<AccountSummaryEntity, Long> {

    List<AccountSummaryEntity> findByAccountIdOrderByCreatedAtDesc(String accountId);
}
