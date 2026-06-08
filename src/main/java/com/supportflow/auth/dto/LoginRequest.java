package com.supportflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "Некорректный email")
        @NotBlank(message = "Email обязателен")
        @Size(max = 120, message = "Email не должен быть длиннее 120 символов")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, max = 72, message = "Пароль должен содержать от 6 до 72 символов")
        String password
) {
}
