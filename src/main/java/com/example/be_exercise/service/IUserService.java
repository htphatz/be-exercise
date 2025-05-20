package com.example.be_exercise.service;

import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUserService {
    UserResponse getById(String id);
    PageDto<UserResponse> getAllUser(int pageNumber, int pageSize);
    PageDto<UserResponse> searchUsers(int pageNumber, int pageSize, String username, String firstName, String lastName, String email) throws JsonProcessingException;
    void deleteById(String id);
    void sendForgotPassword(String email);
    void changePassword(int code, String newPassword);
    void sendVerifyEmail(String email);
    void verifyEmail(String email, String token);
    void exportExcel(HttpServletResponse response) throws IOException;
    void importExcel(MultipartFile file) throws IOException;
}
