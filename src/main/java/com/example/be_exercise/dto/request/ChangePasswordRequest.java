package com.example.be_exercise.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChangePasswordRequest {
    private int code;
    private String email;
    private String newPassword;
}
