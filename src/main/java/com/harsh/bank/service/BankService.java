package com.harsh.bank.service;

import com.harsh.bank.model.Account;
import com.harsh.bank.model.TransactionRecord;
import com.harsh.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safety layer. One ReentrantLock per account serialises operations on the SAME account
 * while different accounts still run in parallel.
 *
 * Why the lock sits OUTSIDE the @Transactional bean: the lock must be released only AFTER the
 * transaction commits. If the lock were inside the transaction, another thread could grab it and
 * read the old balance before our commit became visible.
 *
 * Limitation: in-JVM locks only protect a single application instance. With several instances
 * you would use DB-level locking (pessimistic @Lock or optimistic @Version).
 */
@Service
public class BankService {
    private final AccountTxService tx;
    private final TransactionRepository txns;
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public BankService(AccountTxService tx, TransactionRepository txns) {
        this.tx = tx;
        this.txns = txns;
    }

    private ReentrantLock lockFor(Long id) {
        return locks.computeIfAbsent(id, k -> new ReentrantLock());
    }

    public Account create(String name, BigDecimal initial) { return tx.create(name, initial); }

    public Account get(Long id) { return tx.get(id); }

    public Account deposit(Long id, BigDecimal amt) {
        ReentrantLock l = lockFor(id);
        l.lock();
        try { return tx.deposit(id, amt); } finally { l.unlock(); }
    }

    public Account withdraw(Long id, BigDecimal amt) {
        ReentrantLock l = lockFor(id);
        l.lock();
        try { return tx.withdraw(id, amt); } finally { l.unlock(); }
    }

    public void transfer(Long fromId, Long toId, BigDecimal amt) {
        // Always lock the lower id first. Otherwise A->B and B->A at the same time can deadlock.
        Long first = Math.min(fromId, toId);
        Long second = Math.max(fromId, toId);
        boolean same = first.equals(second);
        ReentrantLock l1 = lockFor(first);
        ReentrantLock l2 = lockFor(second);
        l1.lock();
        try {
            if (!same) l2.lock();
            try { tx.transfer(fromId, toId, amt); }
            finally { if (!same) l2.unlock(); }
        } finally { l1.unlock(); }
    }

    public List<TransactionRecord> history(Long id) {
        tx.get(id); // throws 404 if the account does not exist
        return txns.findByAccountIdOrderByCreatedAtDesc(id);
    }
}
