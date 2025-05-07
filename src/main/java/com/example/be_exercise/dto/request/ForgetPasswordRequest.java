package com.example.be_exercise.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgetPasswordRequest {
    private String email;
}
