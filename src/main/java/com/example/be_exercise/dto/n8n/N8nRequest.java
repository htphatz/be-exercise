package com.example.be_exercise.dto.n8n;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class N8nRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
