package com.supportflow.user.service;

import com.supportflow.user.dto.CreateUserRequest;
import com.supportflow.user.dto.UserResponse;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.exception.UserAlreadyExistsException;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }
        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus()
                ))
                .toList();
    }

    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return  new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}
