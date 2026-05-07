package com.supportflow.user.dto;

import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank
        String name,

        @Email @NotBlank
        String email,

        @NotBlank
        String password
){}