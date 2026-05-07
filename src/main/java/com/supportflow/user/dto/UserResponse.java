package com.supportflow.user.dto;

import com.supportflow.user.enums.UserRole;
import com.supportflow.user.enums.UserStatus;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        UserStatus status
) {
}
