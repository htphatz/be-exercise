package com.example.be_exercise.service;

import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;

public interface IUserService {
    UserResponse getById(String id);
    PageDto<UserResponse> getAllUser(int pageNumber, int pageSize);
    PageDto<UserResponse> searchUsers(int pageNumber, int pageSize, String username, String firstName, String lastName, String email);
    void deleteById(String id);
    void sendForgetPassword(String email);
    void changePassword(int code, String email, String newPassword);
    void sendVerifyEmail(String email);
    void verifyEmail(String email, String token);
}
