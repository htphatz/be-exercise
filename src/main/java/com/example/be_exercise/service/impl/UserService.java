package com.example.be_exercise.service.impl;

import com.example.be_exercise.dto.response.PageDto;
import com.example.be_exercise.dto.response.UserResponse;
import com.example.be_exercise.exception.NotFoundException;
import com.example.be_exercise.mapper.UserMapper;
import com.example.be_exercise.model.User;
import com.example.be_exercise.repository.SearchRepository;
import com.example.be_exercise.repository.UserRepository;
import com.example.be_exercise.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final SearchRepository searchRepository;

    @Override
    public UserResponse getById(String id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return UserMapper.toDto(existingUser);
    }

    @Override
    public PageDto<UserResponse> getAllUser(int pageNumber, int pageSize) {
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> users = userRepository.findAll(pageable);
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
        userRepository.delete(existingUser);
    }
}
