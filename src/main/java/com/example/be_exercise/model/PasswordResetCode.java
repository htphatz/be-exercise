package com.example.be_exercise.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "code", unique = true, nullable = false)
    private int code;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "expiration_time", nullable = false)
    private Instant expirationTime;
}
