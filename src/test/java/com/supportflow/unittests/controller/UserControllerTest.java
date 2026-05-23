package com.supportflow.unittests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportflow.exception.GlobalExceptionHandler;
import com.supportflow.user.controller.UserController;
import com.supportflow.user.dto.UpdateUserRoleRequest;
import com.supportflow.user.dto.UpdateUserStatusRequest;
import com.supportflow.user.dto.UserResponse;
import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import com.supportflow.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /users - возвращает список пользователей")
    void getAllUsers_shouldReturnUsers() throws Exception {
        // Arrange
        UserResponse user = userResponse(
                1L,
                "Test User",
                "user@test.com",
                UserRole.USER,
                UserStatus.ACTIVE
        );

        when(userService.getAllUsers()).thenReturn(List.of(user));

        // Act + Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test User"))
                .andExpect(jsonPath("$[0].email").value("user@test.com"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /users/{id} - возвращает пользователя по id")
    void getUserById_shouldReturnUser() throws Exception {
        // Arrange
        UserResponse user = userResponse(
                1L,
                "Test User",
                "user@test.com",
                UserRole.USER,
                UserStatus.ACTIVE
        );

        when(userService.getUserById(1L)).thenReturn(user);

        // Act + Assert
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("PATCH /users/{id}/role - меняет роль пользователя")
    void updateUserRole_shouldReturnUpdatedUser() throws Exception {
        // Arrange
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.AGENT);

        UserResponse response = userResponse(
                1L,
                "Test User",
                "user@test.com",
                UserRole.AGENT,
                UserStatus.ACTIVE
        );

        when(userService.updateUserRole(anyLong(), any(UpdateUserRoleRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENT"));

        verify(userService).updateUserRole(anyLong(), any(UpdateUserRoleRequest.class));
    }

    @Test
    @DisplayName("PATCH /users/{id}/status - меняет статус пользователя")
    void updateUserStatus_shouldReturnUpdatedUser() throws Exception {
        // Arrange
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.BLOCKED);

        UserResponse response = userResponse(
                1L,
                "Test User",
                "user@test.com",
                UserRole.USER,
                UserStatus.BLOCKED
        );

        when(userService.updateUserStatus(anyLong(), any(UpdateUserStatusRequest.class)))
                .thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch("/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(userService).updateUserStatus(anyLong(), any(UpdateUserStatusRequest.class));
    }

    private UserResponse userResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            UserStatus status
    ) {
        return new UserResponse(
                id,
                name,
                email,
                role,
                status,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }
}