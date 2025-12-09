package com.viana.poc.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "account_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountId;

    @Column(length = 4000)
    private String summary;

    private String classification;
    private Integer riskScore;
    private Instant createdAt;
}
