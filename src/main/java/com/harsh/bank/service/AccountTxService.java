package com.harsh.bank.service;

import com.harsh.bank.exception.AccountNotFoundException;
import com.harsh.bank.exception.InsufficientFundsException;
import com.harsh.bank.model.Account;
import com.harsh.bank.model.TransactionRecord;
import com.harsh.bank.model.TransactionRecord.Type;
import com.harsh.bank.repository.AccountRepository;
import com.harsh.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

/**
 * Each public method is ONE database transaction: if anything throws a RuntimeException,
 * every change (balances + audit rows) is rolled back together. This gives atomicity.
 */
@Service
public class AccountTxService {
    public static final BigDecimal MIN_BALANCE = BigDecimal.ZERO;

    private final AccountRepository accounts;
    private final TransactionRepository txns;

    public AccountTxService(AccountRepository accounts, TransactionRepository txns) {
        this.accounts = accounts;
        this.txns = txns;
    }

    private Account find(Long id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private void checkPositive(BigDecimal amt) {
        if (amt == null || amt.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
    }

    @Transactional
    public Account create(String holderName, BigDecimal initialDeposit) {
        if (initialDeposit == null || initialDeposit.signum() < 0)
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        Account a = new Account();
        a.setHolderName(holderName);
        a.setBalance(initialDeposit);
        return accounts.save(a);
    }

    @Transactional(readOnly = true)
    public Account get(Long id) { return find(id); }

    @Transactional
    public Account deposit(Long id, BigDecimal amt) {
        checkPositive(amt);
        Account a = find(id);
        a.setBalance(a.getBalance().add(amt));
        txns.save(new TransactionRecord(id, Type.DEPOSIT, amt, a.getBalance()));
        return a;
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amt) {
        checkPositive(amt);
        Account a = find(id);
        if (a.getBalance().subtract(amt).compareTo(MIN_BALANCE) < 0)
            throw new InsufficientFundsException("Insufficient funds in account " + id);
        a.setBalance(a.getBalance().subtract(amt));
        txns.save(new TransactionRecord(id, Type.WITHDRAW, amt, a.getBalance()));
        return a;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amt) {
        checkPositive(amt);
        if (fromId.equals(toId)) throw new IllegalArgumentException("Cannot transfer to the same account");
        Account from = find(fromId);
        Account to = find(toId);
        if (from.getBalance().subtract(amt).compareTo(MIN_BALANCE) < 0)
            throw new InsufficientFundsException("Insufficient funds in account " + fromId);
        from.setBalance(from.getBalance().subtract(amt));
        to.setBalance(to.getBalance().add(amt));
        txns.save(new TransactionRecord(fromId, Type.TRANSFER_OUT, amt, from.getBalance()));
        txns.save(new TransactionRecord(toId, Type.TRANSFER_IN, amt, to.getBalance()));
    }
}
