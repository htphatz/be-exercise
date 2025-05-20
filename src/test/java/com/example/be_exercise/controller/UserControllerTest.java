package com.example.be_exercise.controller;

import com.example.be_exercise.dto.request.SendForgetPasswordRequest;
import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.service.impl.UserService;
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

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getById_validId_success() throws Exception {
        String userId = "1";
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username("test123")
                .email("test123@gmail.com")
                .build();

        when(userService.getById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.id").value(userId))
                .andExpect(jsonPath("result.username").value("test123"));
    }

    @Test
    void getAllUsers_validParams_success() throws Exception {
        PageDto<UserResponse> pageDto = new PageDto<>();
        pageDto.setItems(List.of(UserResponse.builder().username("test123").build()));
        pageDto.setPage(1);
        pageDto.setSize(5);

        when(userService.getAllUser(1, 5)).thenReturn(pageDto);

        mockMvc.perform(get("/users")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.page").value(1))
                .andExpect(jsonPath("result.size").value(5))
                .andExpect(jsonPath("result.items[0].username").value("test123"));
    }

    @Test
    void searchUsers_validParams_success() throws Exception {
        PageDto<UserResponse> pageDto = new PageDto<>();
        pageDto.setItems(List.of(UserResponse.builder().username("test123").build()));
        pageDto.setPage(1);
        pageDto.setSize(5);

        when(userService.searchUsers(1, 5, "test123", null, null, null)).thenReturn(pageDto);

        mockMvc.perform(get("/users/search")
                        .param("pageNumber", "1")
                        .param("pageSize", "5")
                        .param("username", "test123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.page").value(1))
                .andExpect(jsonPath("result.size").value(5))
                .andExpect(jsonPath("result.items[0].username").value("test123"));
    }

    @Test
    void deleteById_ValidId_ReturnsEmptyResponse() throws Exception {
        String userId = "1";
        doNothing().when(userService).deleteById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").doesNotExist());
    }

    @Test
    void sendForgotPassword_validRequest_success() throws Exception {
        SendForgetPasswordRequest request = SendForgetPasswordRequest.builder()
                .email("test123@gmail.com")
                .build();

        doNothing().when(userService).sendForgotPassword("test123@gmail.com");

        mockMvc.perform(post("/users/send-forgot-password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(200));
    }
}
