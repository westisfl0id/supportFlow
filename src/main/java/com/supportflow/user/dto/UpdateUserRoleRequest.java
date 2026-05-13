package com.supportflow.user.dto;

import com.supportflow.user.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull
        UserRole role
) {
}
