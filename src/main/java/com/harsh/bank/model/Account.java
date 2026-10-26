package com.harsh.bank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String holderName;

    // BigDecimal, never double: binary floating point cannot represent money exactly
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    public Long getId() { return id; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
