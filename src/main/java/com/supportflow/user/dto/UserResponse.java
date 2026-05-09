package com.supportflow.user.dto;

import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
}
