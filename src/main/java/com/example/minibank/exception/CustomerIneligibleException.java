package com.example.minibank.exception;

public class CustomerIneligibleException extends RuntimeException {
    public CustomerIneligibleException(String message) {
        super(message);
    }
}
