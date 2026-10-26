package com.harsh.bank.repository;

import com.harsh.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {}
