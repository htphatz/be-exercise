package com.example.be_exercise.controller;

import com.example.be_exercise.dto.request.LoginRequest;
import com.example.be_exercise.dto.request.LogoutRequest;
import com.example.be_exercise.dto.request.RegisterRequest;
import com.example.be_exercise.dto.request.RequestTokenRequest;
import com.example.be_exercise.dto.response.LoginResponse;
import com.example.be_exercise.dto.response.RefreshTokenResponse;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.service.impl.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_validRequest_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("test123")
                .password("test123")
                .email("test123@gmail.com")
                .firstName("test123")
                .lastName("test123")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .username("test123")
                .email("test123@gmail.com")
                .firstName("test123")
                .lastName("test123")
                .build();

        when(authService.register(any())).thenReturn(userResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(200))
                .andExpect(jsonPath("result.email").value("test123@gmail.com"));
    }

    @Test
    void register_usernameInvalid_fail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .password("test123")
                .email("test123@gmail.com")
                .firstName("test123")
                .lastName("test123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validRequest_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("test123")
                .password("test123")
                .build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(200))
                .andExpect(jsonPath("result.accessToken").value("accessToken"))
                .andExpect(jsonPath("result.refreshToken").value("refreshToken"));
    }

    @Test
    void logout_validRequest_success() throws Exception {
        LogoutRequest request = LogoutRequest.builder()
                .token("accessToken")
                .build();

        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(200));
    }

    @Test
    void refresh_validRequest_success() throws Exception {
        RequestTokenRequest request = RequestTokenRequest.builder()
                .refreshToken("refreshToken")
                .build();

        RefreshTokenResponse response = RefreshTokenResponse.builder()
                .userId("userId")
                .accessToken("accessToken")
                .build();

        when(authService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.userId").value("userIds"))
                .andExpect(jsonPath("result.accessToken").value("accessToken"));
    }
}
