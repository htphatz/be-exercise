package com.example.be_exercise.service;

import com.example.be_exercise.dto.request.*;
import com.example.be_exercise.dto.response.IntrospectResponse;
import com.example.be_exercise.dto.response.LoginResponse;
import com.example.be_exercise.dto.response.RefreshTokenResponse;
import com.example.be_exercise.dto.response.UserResponse;

public interface IAuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    void logout(LogoutRequest request);
    RefreshTokenResponse refreshToken(RequestTokenRequest request);
}
