package com.harsh.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class TransactionRecord {
    public enum Type { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private LocalDateTime createdAt = LocalDateTime.now();

    public TransactionRecord() {}

    public TransactionRecord(Long accountId, Type type, BigDecimal amount, BigDecimal balanceAfter) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public Type getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
