package com.supportflow.unittests.service;

import com.supportflow.auth.dto.AuthResponse;
import com.supportflow.auth.dto.LoginRequest;
import com.supportflow.auth.dto.RegisterRequest;
import com.supportflow.auth.exception.EmailAlreadyExistsException;
import com.supportflow.auth.exception.InvalidCredentialsException;
import com.supportflow.auth.exception.UserBlockedException;
import com.supportflow.auth.service.AuthService;
import com.supportflow.security.jwt.JwtService;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register - успешная регистрация пользователя")
    void register_shouldReturnAuthResponse_whenEmailIsUnique() {
        // Arrange — подготавливаем входные данные и поведение зависимостей
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "user@test.com",
                "123456"
        );

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        // В реальной JPA id появляется после save().
        // В unit-тесте через Mockito имитируем это поведение вручную.
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("jwt-token");

        // Act — вызываем тестируемый метод
        AuthResponse response = authService.register(request);

        // Assert — проверяем результат
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(1L, response.userId());
        assertEquals("Test User", response.name());
        assertEquals("user@test.com", response.email());
        assertEquals(UserRole.USER, response.role());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();

        assertEquals("Test User", savedUser.getName());
        assertEquals("user@test.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getRole());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());

        verify(userRepository).existsByEmail("user@test.com");
        verify(passwordEncoder).encode("123456");
        verify(jwtService).generateToken(any(UserEntity.class));
    }

    @Test
    @DisplayName("register - ошибка при повторном email")
    void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "user@test.com",
                "123456"
        );

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        // Act + Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository).existsByEmail("user@test.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtService, never()).generateToken(any(UserEntity.class));
    }

    @Test
    @DisplayName("login - успешный вход пользователя")
    void login_shouldReturnAuthResponse_whenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("user@test.com", "123456");

        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("Test User")
                .email("user@test.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(1L, response.userId());
        assertEquals("Test User", response.name());
        assertEquals("user@test.com", response.email());
        assertEquals(UserRole.USER, response.role());

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("login - ошибка если пользователь не найден")
    void login_shouldThrowInvalidCredentialsException_whenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("user@test.com", "123456");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any(UserEntity.class));
    }

    @Test
    @DisplayName("login - ошибка при неверном пароле")
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsInvalid() {
        // Arrange
        LoginRequest request = new LoginRequest("user@test.com", "wrong-password");

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded-password")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // Act + Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verify(jwtService, never()).generateToken(any(UserEntity.class));
    }

    @Test
    @DisplayName("login - ошибка при заблокированном пользователе")
    void login_shouldThrowUserBlockedException_whenUserIsBlocked() {
        // Arrange
        LoginRequest request = new LoginRequest("user@test.com", "123456");

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded-password")
                .status(UserStatus.BLOCKED)
                .build();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);

        // Act + Assert
        assertThrows(
                UserBlockedException.class,
                () -> authService.login(request)
        );

        verify(userRepository).findByEmail("user@test.com");
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(jwtService, never()).generateToken(any(UserEntity.class));
    }
}
