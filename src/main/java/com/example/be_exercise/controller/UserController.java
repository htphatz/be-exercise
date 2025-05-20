package com.example.be_exercise.controller;

import com.example.be_exercise.dto.request.ChangePasswordRequest;
import com.example.be_exercise.dto.request.SendForgetPasswordRequest;
import com.example.be_exercise.dto.request.SendVerifyEmailRequest;
import com.example.be_exercise.dto.request.VerifyEmailRequest;
import com.example.be_exercise.dto.response.APIResponse;
import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.service.impl.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService userService;

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
    ) throws JsonProcessingException {
        PageDto<UserResponse> result = userService.searchUsers(pageNumber, pageSize, username, firstName, lastName, email);
        return APIResponse.<PageDto<UserResponse>>builder().result(result).build();
    }

    @DeleteMapping("{id}")
    public APIResponse<Void> deleteById(@PathVariable("id") String id) {
        userService.deleteById(id);
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("send-forgot-password")
    public APIResponse<Void> sendForgotPassword(@Valid @RequestBody SendForgetPasswordRequest request) {
        userService.sendForgotPassword(request.getEmail());
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("change-password")
    public APIResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request.getCode(), request.getNewPassword());
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("send-verify-email")
    public APIResponse<Void> sendVerifyEmail(@Valid @RequestBody SendVerifyEmailRequest request) {
        userService.sendVerifyEmail(request.getEmail());
        return APIResponse.<Void>builder().build();
    }

    @PostMapping("verify-email")
    public APIResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request.getToken(), request.getEmail());
        return APIResponse.<Void>builder().build();
    }

    @GetMapping("export-excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        userService.exportExcel(response);
    }

    @PostMapping(value = "import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<Void> importExcel(@RequestPart MultipartFile file) throws IOException {
        userService.importExcel(file);
        return APIResponse.<Void>builder().build();
    }
}
