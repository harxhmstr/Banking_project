package com.harsh.bank.repository;

import com.harsh.bank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
