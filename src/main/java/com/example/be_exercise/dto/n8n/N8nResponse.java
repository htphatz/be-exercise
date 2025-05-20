package com.example.be_exercise.dto.n8n;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class N8nResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Instant timestamp = Instant.now();
}
