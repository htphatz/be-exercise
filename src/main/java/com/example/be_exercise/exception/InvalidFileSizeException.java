package com.example.be_exercise.exception;

public class InvalidFileSizeException extends RuntimeException {
    public InvalidFileSizeException(String message) {
        super(message);
    }
}
