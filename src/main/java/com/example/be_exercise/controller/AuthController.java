package com.example.be_exercise.controller;

import com.example.be_exercise.dto.request.LoginRequest;
import com.example.be_exercise.dto.request.LogoutRequest;
import com.example.be_exercise.dto.request.RegisterRequest;
import com.example.be_exercise.dto.request.RequestTokenRequest;
import com.example.be_exercise.dto.response.APIResponse;
import com.example.be_exercise.dto.response.LoginResponse;
import com.example.be_exercise.dto.response.RefreshTokenResponse;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("register")
    public APIResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse result = authService.register(request);
        return APIResponse.<UserResponse>builder().result(result).build();
    }

    @PostMapping("login")
    public APIResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return APIResponse.<LoginResponse>builder().result(result).build();
    }

    @PostMapping("logout")
    public APIResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("refresh")
    public APIResponse<RefreshTokenResponse> refresh(@Valid @RequestBody RequestTokenRequest request) {
        RefreshTokenResponse result = authService.refreshToken(request);
        return APIResponse.<RefreshTokenResponse>builder().result(result).build();
    }
}
