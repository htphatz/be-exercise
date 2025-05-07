package com.example.be_exercise.exception;

public class InvalidPasswordResetCodeException extends RuntimeException {
    public InvalidPasswordResetCodeException(String message) {
        super(message);
    }
}
