package com.harsh.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> insufficient(InsufficientFundsException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid request: " + e.getMessage()));
    }
}
