package com.supportflow.unittests.service;

import com.supportflow.security.CurrentUserService;
import com.supportflow.user.dto.CreateUserRequest;
import com.supportflow.user.dto.UpdateUserRoleRequest;
import com.supportflow.user.dto.UpdateUserStatusRequest;
import com.supportflow.user.dto.UserResponse;
import com.supportflow.user.entity.UserEntity;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.exception.UserAlreadyExistsException;
import com.supportflow.user.exception.UserNotFoundException;
import com.supportflow.user.repository.UserRepository;
import com.supportflow.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("createUser - успешное создание пользователя")
    void createUser_shouldCreateUser_whenEmailIsUnique() {
        CreateUserRequest request = new CreateUserRequest(
                "Test User",
                "user@test.com",
                "123456"
        );

        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(createdAt);
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Test User", response.name());
        assertEquals("user@test.com", response.email());
        assertEquals(UserRole.USER, response.role());
        assertEquals(UserStatus.ACTIVE, response.status());
        assertEquals(createdAt, response.createdAt());

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
    }

    @Test
    @DisplayName("createUser - ошибка при повторном email")
    void createUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "Test User",
                "user@test.com",
                "123456"
        );

        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository).existsByEmail("user@test.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("getAllUsers - возвращает список пользователей")
    void getAllUsers_shouldReturnUsers() {
        UserEntity user1 = user(1L, "User One", "user1@test.com", UserRole.USER, UserStatus.ACTIVE);
        UserEntity user2 = user(2L, "Agent One", "agent@test.com", UserRole.AGENT, UserStatus.ACTIVE);

        when(userRepository.findAllByOrderByStatusAscIdAsc())
                .thenReturn(List.of(user1, user2));

        List<UserResponse> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("User One", response.get(0).name());
        assertEquals(UserRole.AGENT, response.get(1).role());

        verify(userRepository).findAllByOrderByStatusAscIdAsc();
        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("getUserById - возвращает пользователя по id")
    void getUserById_shouldReturnUser_whenUserExists() {
        UserEntity user = user(1L, "Test User", "user@test.com", UserRole.USER, UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertEquals(1L, response.id());
        assertEquals("Test User", response.name());
        assertEquals("user@test.com", response.email());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById - ошибка если пользователь не найден")
    void getUserById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(1L)
        );

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("updateUserStatus - меняет статус пользователя")
    void updateUserStatus_shouldUpdateStatus_whenUserExists() {
        UserEntity user = user(1L, "Test User", "user@test.com", UserRole.USER, UserStatus.ACTIVE);
        UserEntity admin = user(99L, "Admin", "admin@test.com", UserRole.ADMIN, UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentUser()).thenReturn(admin);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.BLOCKED);

        UserResponse response = userService.updateUserStatus(1L, request);

        assertEquals(UserStatus.BLOCKED, response.status());
        assertEquals(UserStatus.BLOCKED, user.getStatus());

        verify(userRepository).findById(1L);
        verify(currentUserService).getCurrentUser();
    }

    @Test
    @DisplayName("updateUserRole - меняет роль пользователя")
    void updateUserRole_shouldUpdateRole_whenUserExists() {
        UserEntity user = user(1L, "Test User", "user@test.com", UserRole.USER, UserStatus.ACTIVE);
        UserEntity admin = user(99L, "Admin", "admin@test.com", UserRole.ADMIN, UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentUser()).thenReturn(admin);

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.AGENT);

        UserResponse response = userService.updateUserRole(1L, request);

        assertEquals(UserRole.AGENT, response.role());
        assertEquals(UserRole.AGENT, user.getRole());

        verify(userRepository).findById(1L);
        verify(currentUserService).getCurrentUser();
    }

    private UserEntity user(Long id, String name, String email, UserRole role, UserStatus status) {
        return UserEntity.builder()
                .id(id)
                .name(name)
                .email(email)
                .password("encoded-password")
                .role(role)
                .status(status)
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }
}