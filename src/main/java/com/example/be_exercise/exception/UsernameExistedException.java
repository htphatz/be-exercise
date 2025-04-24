package com.example.be_exercise.exception;

public class UsernameExistedException extends RuntimeException {
    public UsernameExistedException(String message) {
        super(message);
    }
}
