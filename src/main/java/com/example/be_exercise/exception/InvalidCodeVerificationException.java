package com.example.be_exercise.exception;

public class InvalidCodeVerificationException extends RuntimeException {
    public InvalidCodeVerificationException(String message) {
        super(message);
    }
}
