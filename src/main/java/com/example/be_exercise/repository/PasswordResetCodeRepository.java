package com.example.be_exercise.repository;

import com.example.be_exercise.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, String> {
    Optional<PasswordResetCode> findByCode(int code);
}
