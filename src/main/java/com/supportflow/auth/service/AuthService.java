package com.supportflow.auth.service;

import com.supportflow.auth.dto.AuthResponse;
import com.supportflow.auth.dto.LoginRequest;
import com.supportflow.auth.dto.RegisterRequest;
import com.supportflow.auth.exception.EmailAlreadyExistsException;
import com.supportflow.auth.exception.InvalidCredentialsException;
import com.supportflow.auth.exception.UserBlockedException;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException();
        }

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

    }
}
