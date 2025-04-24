package com.example.be_exercise.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String fullName;
    private String email;
}
