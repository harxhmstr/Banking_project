package com.harsh.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) { super("Account not found: " + id); }
}
