package com.example.be_exercise.service.impl;

import com.example.be_exercise.dto.brevo.EmailParam;
import com.example.be_exercise.dto.brevo.EmailRequest;
import com.example.be_exercise.dto.brevo.RecipientRequest;
import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.exception.*;
import com.example.be_exercise.mapper.UserMapper;
import com.example.be_exercise.model.PasswordResetCode;
import com.example.be_exercise.model.Role;
import com.example.be_exercise.model.User;
import com.example.be_exercise.repository.PasswordResetCodeRepository;
import com.example.be_exercise.repository.RoleRepository;
import com.example.be_exercise.repository.SearchRepository;
import com.example.be_exercise.repository.UserRepository;
import com.example.be_exercise.repository.httpclient.BrevoClient;
import com.example.be_exercise.service.IUserService;
import com.example.be_exercise.utils.TokenGeneratorUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SearchRepository searchRepository;
    private final RedisService<String, String> redisService;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorUtils tokenGeneratorUtils;
    private final BrevoClient brevoClient;
    private final ObjectMapper objectMapper;

    private final static String CONTENT_TYPE_FILE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.template-forget-password}")
    private int templateForgetPassword;

    @Value("${brevo.template-verify-email}")
    private int templateVerifyEmail;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        if (existingUser.isDelete()) {
            throw new NotFoundException("User with id " + id + " is deleted");
        }

        return UserMapper.toDto(existingUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<UserResponse> getAllUser(int pageNumber, int pageSize) {
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> users = userRepository.findAllByIsDeleteFalse(pageable);
        return PageDto.of(users).map(UserMapper.toDtoFunction());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PageDto<UserResponse> searchUsers(int pageNumber, int pageSize, String username,
                                             String firstName, String lastName, String email) throws JsonProcessingException {
        String key = getKeyForSearchUsers(pageNumber, pageSize, username, email, firstName, lastName);
        if (redisService.get(key) == null) {
            Page<User> users = searchRepository.searchUsers(pageNumber, pageSize, username, firstName, lastName, email);
            PageDto<UserResponse> result = PageDto.of(users).map(UserMapper.toDtoFunction());
            String json = objectMapper.writeValueAsString(result);
            redisService.set(key, json);
            redisService.setTimeToLive(key, 10L);
            return result;
        }
        else {
            String json = redisService.get(key);
            return objectMapper.readValue(json, new TypeReference<PageDto<UserResponse>>() {});
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        existingUser.setDelete(true);
        userRepository.save(existingUser);
    }

    @Override
    public void sendVerifyEmail(String email) {
        User exitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));

        RecipientRequest recipient = RecipientRequest.builder()
                .email(email)
                .build();

        String code = tokenGeneratorUtils.generateVerificationToken();
        String htmlLink = "http://localhost:4200/auth/verify-email?token=" + code + "&email=" + email;
        EmailParam params = EmailParam.builder()
                .code(htmlLink)
                .email(email)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .to(List.of(recipient))
                .templateId(templateVerifyEmail)
                .params(params)
                .build();

        brevoClient.sendEmail(apiKey, emailRequest);

        exitingUser.setVerificationToken(code);
        exitingUser.setTokenExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));
        userRepository.save(exitingUser);
    }

    @Override
    public void sendForgotPassword(String email) {
        User exitingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));

        RecipientRequest recipient = RecipientRequest.builder()
                .email(email)
                .build();

        int code = tokenGeneratorUtils.generatePasswordRestToken();
        EmailParam params = EmailParam.builder()
                .code(code)
                .email(email)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .to(List.of(recipient))
                .templateId(templateForgetPassword)
                .params(params)
                .build();

        // Save the password reset code to database
        savePasswordResetCode(exitingUser, code);

        brevoClient.sendEmail(apiKey, emailRequest);
    }

    @Override
    public void changePassword(int code, String newPassword) {
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

    @Override
    public void verifyEmail(String token, String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));

        if (existingUser.getVerificationToken() == null || !existingUser.getVerificationToken().equals(token)) {
            throw new InvalidCodeVerificationException("Invalid token");
        }

        if (existingUser.getTokenExpiration().isBefore(Instant.now())) {
            throw new InvalidCodeVerificationException("Token verification already expired");
        }

        existingUser.setVerify(true);
        existingUser.setVerificationToken(null);
        existingUser.setTokenExpiration(null);
        userRepository.save(existingUser);
    }

    private void savePasswordResetCode(User user, int code) {
        PasswordResetCode passwordResetCode = PasswordResetCode.builder()
                .userId(user.getId())
                .code(code)
                .expirationTime(Instant.now().plus(5, ChronoUnit.MINUTES))
                .build();

        passwordResetCodeRepository.save(passwordResetCode);
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<User> users = userRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Username");
        header.createCell(2).setCellValue("First name");
        header.createCell(3).setCellValue("Last name");
        header.createCell(4).setCellValue("Email");

        int rowNum = 1;
        for (User user: users) {
            if (user.isDelete())
                continue;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getFirstName());
            row.createCell(3).setCellValue(user.getLastName());
            row.createCell(4).setCellValue(user.getEmail());
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    @Override
    @Transactional(rollbackFor = { Exception.class })
    public void importExcel(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new NotFoundException("File not found");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        log.info(contentType);
        if (!filename.endsWith(".xls") && !filename.endsWith(".xlsx") &&!contentType.equals(CONTENT_TYPE_FILE)) {
            throw new InvalidFileException("Invalid file format");
        }

        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new InvalidFileSizeException("File's size muse be <= 2MB");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<User> users = new ArrayList<>();
        for (Row row: sheet) {
            if (row.getRowNum() == 0) continue;

            User user = User.builder().build();

            // Check if username existed
            String username = row.getCell(0).getStringCellValue();
            user.setUsername(username);
            if (userRepository.existsByUsername(username)) {
                throw new UsernameExistedException("Username " + username + " already existed");
            }

            user.setPassword(passwordEncoder.encode(row.getCell(1).getStringCellValue()));
            user.setFirstName(row.getCell(2).getStringCellValue());
            user.setLastName(row.getCell(3).getStringCellValue());
            user.setEmail(row.getCell(4).getStringCellValue());

            user.setDelete(false);
            user.setVerify(false);
            user.setTokenExpiration(null);
            user.setVerificationToken(null);

            // Set role for user
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName(Role.USER).ifPresent(roles::add);
            user.setRoles(roles);

            users.add(user);
        }
        userRepository.saveAll(users);
    }

    private String getKeyForSearchUsers(int pageNumber, int pageSize, String username, String firstName, String lastName, String email) {
        return String.format("search_users:%s:%s:%s:%s:%s", pageNumber, pageSize, username, firstName, lastName, email);
    }
}
