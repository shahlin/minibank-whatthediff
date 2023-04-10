package com.example.minibank.exception;

public class AccountExistsException extends RuntimeException {
    public AccountExistsException() {
        super("Account already exists for the customer");
    }
}
