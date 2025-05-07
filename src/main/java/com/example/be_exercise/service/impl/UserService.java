package com.example.be_exercise.service.impl;

import com.example.be_exercise.dto.brevo.EmailParam;
import com.example.be_exercise.dto.brevo.EmailRequest;
import com.example.be_exercise.dto.brevo.RecipientRequest;
import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.exception.InvalidPasswordResetCodeException;
import com.example.be_exercise.exception.NotFoundException;
import com.example.be_exercise.mapper.UserMapper;
import com.example.be_exercise.model.PasswordResetCode;
import com.example.be_exercise.model.User;
import com.example.be_exercise.repository.PasswordResetCodeRepository;
import com.example.be_exercise.repository.SearchRepository;
import com.example.be_exercise.repository.UserRepository;
import com.example.be_exercise.repository.httpclient.BrevoClient;
import com.example.be_exercise.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final BrevoClient brevoClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.template-id}")
    private int templateId;

    @Override
    public UserResponse getById(String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        if (existingUser.isDelete()) {
            throw new NotFoundException("User with id " + id + " is deleted");
        }

        return UserMapper.toDto(existingUser);
    }

    @Override
    public PageDto<UserResponse> getAllUser(int pageNumber, int pageSize) {
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> users = userRepository.findAllByIsDeleteFalse(pageable);
        return PageDto.of(users).map(UserMapper.toDtoFunction());
    }

    @Override
    public PageDto<UserResponse> searchUsers(int pageNumber, int pageSize, String username, String firstName, String lastName, String email) {
        Page<User> users = searchRepository.searchUsers(pageNumber, pageSize, username, firstName, lastName, email);
        return PageDto.of(users).map(UserMapper.toDtoFunction());
    }

    @Override
    public void deleteById(String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        existingUser.setDelete(true);
        userRepository.save(existingUser);
    }

    @Override
    public void forgetPassword(String email) {
        User exitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));

        RecipientRequest recipient = RecipientRequest.builder()
                .email(email)
                .build();

        int code = randomCode();
        EmailParam params = EmailParam.builder()
                .code(code)
                .email(email)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .to(List.of(recipient))
                .templateId(templateId)
                .params(params)
                .build();

        // Save the password reset code to database
        savePasswordResetCode(exitingUser, code);

        brevoClient.sendEmail(apiKey, emailRequest);
    }

    @Override
    public void changePassword(int code, String email, String newPassword) {
        if (!userRepository.existsByEmail(email)) {
            throw new NotFoundException("User with email " + email + " not found");
        }

        PasswordResetCode passwordResetCode = passwordResetCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Password reset code " + code + " not found"));

        // Check expiration time
        if (passwordResetCode.getExpirationTime().isBefore(Instant.now())) {
            throw new InvalidPasswordResetCodeException("Password reset code " + code + " already expired");
        }

        // Check matching user id between password reset code and user
        User user = userRepository.findById(passwordResetCode.getUserId())
                .orElseThrow(() -> new NotFoundException("User with id " + passwordResetCode.getUserId() + " not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void savePasswordResetCode(User user, int code) {
        PasswordResetCode passwordResetCode = PasswordResetCode.builder()
                .userId(user.getId())
                .code(code)
                .expirationTime(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();

        passwordResetCodeRepository.save(passwordResetCode);
    }

    private int randomCode() {
        return (int) (Math.random() * 100000000);
    }
}
