package com.example.be_exercise.dto.brevo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecipientRequest {
    private String email;
    private String name;
}
