package com.example.minibank.exception;

import java.time.LocalDateTime;

public class ErrorMessage {
    private int statusCode;
    private LocalDateTime timestamp;
    private String message;

    public ErrorMessage(int statusCode, LocalDateTime timestamp, String message) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
