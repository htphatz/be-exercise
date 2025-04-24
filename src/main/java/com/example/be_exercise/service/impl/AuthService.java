package com.example.be_exercise.service.impl;

import com.example.be_exercise.dto.request.*;
import com.example.be_exercise.dto.response.IntrospectResponse;
import com.example.be_exercise.dto.response.LoginResponse;
import com.example.be_exercise.dto.response.RefreshTokenResponse;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.exception.*;
import com.example.be_exercise.mapper.UserMapper;
import com.example.be_exercise.model.InvalidToken;
import com.example.be_exercise.model.Role;
import com.example.be_exercise.model.User;
import com.example.be_exercise.repository.InvalidTokenRepository;
import com.example.be_exercise.repository.RoleRepository;
import com.example.be_exercise.repository.UserRepository;
import com.example.be_exercise.service.IAuthService;
import com.example.be_exercise.util.JwtUtils;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InvalidTokenRepository invalidTokenRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameExistedException("Username " + request.getUsername() + " already existed");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailExistedException("Email " + request.getEmail() + " already existed");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .build();

        Set<Role> roles = new HashSet<>();
        roleRepository.findByName(Role.USER).ifPresent(roles::add);
        user.setRoles(roles);

        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User existingUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username " + request.getUsername() + " not found"));

        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }

        String accessToken = jwtUtils.generateAccessToken(existingUser);
        String refreshToken = jwtUtils.generateRefreshToken(existingUser);

        userRepository.save(existingUser);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        boolean isValid = true;
        try {
            jwtUtils.verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().isValid(isValid).build();
    }

    @Override
    public void logout(LogoutRequest request) {
        try {
            SignedJWT signedJWT = jwtUtils.verifyToken(request.getToken());
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Instant expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

            InvalidToken invalidToken = InvalidToken.builder()
                    .id(jwtId)
                    .expirationTime(expirationTime)
                    .build();
            invalidTokenRepository.save(invalidToken);
        } catch (Exception e) {
            log.info("Token expired");
        }
    }

    @Override
    public RefreshTokenResponse refreshToken(RequestTokenRequest request) {
        String username = jwtUtils.extractSubject(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username " + username + " not found"));

        try {
            SignedJWT token = jwtUtils.verifyToken(request.getRefreshToken());

            // Check if refresh token is invalid
            String jwtId = token.getJWTClaimsSet().getJWTID();
            if(invalidTokenRepository.existsById(jwtId))
                throw new InvalidRefreshTokenException("Invalid refresh token");

            String accessToken = jwtUtils.generateAccessToken(user);
            return RefreshTokenResponse.builder()
                    .accessToken(accessToken)
                    .userId(user.getId())
                    .build();
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }
}
