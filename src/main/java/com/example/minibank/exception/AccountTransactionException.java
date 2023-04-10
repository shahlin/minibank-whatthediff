package com.example.minibank.exception;

public class AccountTransactionException extends RuntimeException {
    public AccountTransactionException(String message) {
        super(message);
    }
}
