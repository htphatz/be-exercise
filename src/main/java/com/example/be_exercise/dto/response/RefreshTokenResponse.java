package com.example.be_exercise.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResponse {
    private String userId;
    private String accessToken;
}
