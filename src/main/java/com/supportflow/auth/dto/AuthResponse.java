package com.supportflow.auth.dto;

import com.supportflow.user.enums.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        UserRole role
) {
}
