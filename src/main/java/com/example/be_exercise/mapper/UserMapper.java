package com.example.be_exercise.mapper;

import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.model.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class UserMapper {
    public static Function<User, UserResponse> toDtoFunction() {
        return user -> UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}
