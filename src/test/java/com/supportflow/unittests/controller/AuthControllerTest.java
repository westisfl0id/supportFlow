package com.supportflow.unittests.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportflow.auth.controller.AuthController;
import com.supportflow.auth.dto.AuthResponse;
import com.supportflow.auth.dto.LoginRequest;
import com.supportflow.auth.dto.RegisterRequest;
import com.supportflow.auth.service.AuthService;
import com.supportflow.exception.GlobalExceptionHandler;
import com.supportflow.user.enums.UserRole;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/register - успешная регистрация")
    void register_shouldReturnCreatedAndAuthResponse() throws Exception {
        // Arrange — готовим запрос и ответ сервиса
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "user@test.com",
                "123456"
        );

        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "Test User",
                "user@test.com",
                UserRole.USER
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act + Assert — отправляем HTTP-запрос и проверяем JSON-ответ
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - успешный вход")
    void login_shouldReturnOkAndAuthResponse() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(
                "user@test.com",
                "123456"
        );

        AuthResponse response = new AuthResponse(
                "jwt-token",
                1L,
                "Test User",
                "user@test.com",
                UserRole.USER
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - ошибка валидации при пустом email")
    void login_shouldReturnBadRequest_whenEmailIsBlank() throws Exception {
        // Arrange — email пустой, поэтому контроллер должен вернуть 400 до вызова сервиса
        LoginRequest request = new LoginRequest(
                "",
                "123456"
        );

        // Act + Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }
}