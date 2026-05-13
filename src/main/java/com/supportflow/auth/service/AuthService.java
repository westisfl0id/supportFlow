package com.supportflow.auth.service;

import com.supportflow.auth.dto.AuthResponse;
import com.supportflow.auth.dto.LoginRequest;
import com.supportflow.auth.dto.RegisterRequest;
import com.supportflow.auth.exception.EmailAlreadyExistsException;
import com.supportflow.auth.exception.InvalidCredentialsException;
import com.supportflow.auth.exception.UserBlockedException;
import com.supportflow.security.jwt.JwtService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        log.info("User register: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

        String token = jwtService.generateToken(user);

        return mapToAuthResponse(user, token);
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

        log.info("User logged in: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

        String token = jwtService.generateToken(user);

        return  mapToAuthResponse(user, token);
    }

    private AuthResponse mapToAuthResponse(UserEntity user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
