package com.example.be_exercise.exception;

public class EmailExistedException extends RuntimeException {
    public EmailExistedException(String message) {
        super(message);
    }
}
