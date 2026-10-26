package com.harsh.bank.controller;

import com.harsh.bank.model.Account;
import com.harsh.bank.model.TransactionRecord;
import com.harsh.bank.service.BankService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankController {
    private final BankService bank;

    public BankController(BankService bank) { this.bank = bank; }

    public record CreateRequest(@NotBlank String holderName,
                                @NotNull @DecimalMin("0.00") BigDecimal initialDeposit) {}
    public record AmountRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record TransferRequest(@NotNull Long fromId, @NotNull Long toId,
                                  @NotNull @DecimalMin("0.01") BigDecimal amount) {}

    @PostMapping("/accounts")
    public ResponseEntity<Account> create(@Valid @RequestBody CreateRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bank.create(r.holderName(), r.initialDeposit()));
    }

    @GetMapping("/accounts/{id}")
    public Account get(@PathVariable Long id) { return bank.get(id); }

    @PostMapping("/accounts/{id}/deposit")
    public Account deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest r) {
        return bank.deposit(id, r.amount());
    }

    @PostMapping("/accounts/{id}/withdraw")
    public Account withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest r) {
        return bank.withdraw(id, r.amount());
    }

    @PostMapping("/transfer")
    public Map<String, String> transfer(@Valid @RequestBody TransferRequest r) {
        bank.transfer(r.fromId(), r.toId(), r.amount());
        return Map.of("status", "Transfer successful");
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<TransactionRecord> history(@PathVariable Long id) { return bank.history(id); }
}
