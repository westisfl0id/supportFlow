package com.supportflow.user.service;

import com.supportflow.user.dto.CreateUserRequest;
import com.supportflow.user.dto.UpdateUserStatusRequest;
import com.supportflow.user.dto.UserResponse;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.exception.UserAlreadyExistsException;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        return map(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = findUserById(id);
        return map(user);
    }

    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request) {
        UserEntity user = findUserById(id);
        user.setStatus(request.status());
        return map(user);
    }

    private UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse map(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
