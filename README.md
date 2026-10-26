# Bank Management System - Java Backend

Java backend simulating core banking: accounts, deposits, withdrawals, fund transfers, audit history.
Focus: transactional integrity and thread-safe concurrent access.

## Tech
Java 17, Spring Boot 3, Spring Data JPA, MySQL, ReentrantLock concurrency, JUnit 5, Postman

## Architecture
Controller -> BankService (per-account locks) -> AccountTxService (@Transactional) -> Repository -> MySQL

## Run
1. Install MySQL, set the password in `src/main/resources/application.properties`
2. `mvn spring-boot:run`  (database `bankdb` is created automatically)
3. `mvn test`  (uses in-memory H2, no MySQL needed)

## API
| Method | Endpoint | Body |
|---|---|---|
| POST | /api/accounts | {"holderName":"Harsh","initialDeposit":1000} |
| GET | /api/accounts/{id} | |
| POST | /api/accounts/{id}/deposit | {"amount":500} |
| POST | /api/accounts/{id}/withdraw | {"amount":200} |
| POST | /api/transfer | {"fromId":1,"toId":2,"amount":300} |
| GET | /api/accounts/{id}/transactions | |
