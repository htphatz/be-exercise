package com.example.be_exercise.controller;

import com.example.be_exercise.dto.request.ChangePasswordRequest;
import com.example.be_exercise.dto.request.SendForgetPasswordRequest;
import com.example.be_exercise.dto.request.SendVerifyEmailRequest;
import com.example.be_exercise.dto.response.APIResponse;
import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("{id}")
    public APIResponse<UserResponse> getById(@PathVariable("id") String id) {
        UserResponse result = userService.getById(id);
        return APIResponse.<UserResponse>builder().result(result).build();
    }

    @GetMapping
    public APIResponse<PageDto<UserResponse>> getAllUsers(
            @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize
    ) {
        PageDto<UserResponse> result = userService.getAllUser(pageNumber, pageSize);
        return APIResponse.<PageDto<UserResponse>>builder().result(result).build();
    }

    @GetMapping("search")
    public APIResponse<PageDto<UserResponse>> searchUsers(
            @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "firstName", required = false) String firstName,
            @RequestParam(name = "lastName", required = false) String lastName,
            @RequestParam(name = "email", required = false) String email
    ) {
        PageDto<UserResponse> result = userService.searchUsers(pageNumber, pageSize, username, firstName, lastName, email);
        return APIResponse.<PageDto<UserResponse>>builder().result(result).build();
    }

    @DeleteMapping("{id}")
    public APIResponse<Void> deleteById(@PathVariable("id") String id) {
        userService.deleteById(id);
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("send-forget-password")
    public APIResponse<Void> senndForgetPassword(@Valid @RequestBody SendForgetPasswordRequest request) {
        userService.sendForgetPassword(request.getEmail());
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("change-password")
    public APIResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.getCode(), request.getEmail(), request.getNewPassword());
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("send-verify-email")
    public APIResponse<Void> sendVerifyEmail(@Valid @RequestBody SendVerifyEmailRequest request) {
        userService.sendVerifyEmail(request.getEmail());
        return APIResponse.<Void>builder().build();
    }

    @GetMapping("verify-email")
    public APIResponse<Void> verifyEmail(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "token") String token) {
        userService.verifyEmail(email, token);
        return APIResponse.<Void>builder().build();
    }
}
