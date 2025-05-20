package com.example.be_exercise.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TokenGeneratorUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 32;

    public String generateVerificationToken() {
        Random random = new Random();
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            token.append(CHARACTERS.charAt(index));
        }
        return token.toString();
    }

    public int generatePasswordRestToken() {
        return (int) (Math.random() * 100000000);
    }
}
