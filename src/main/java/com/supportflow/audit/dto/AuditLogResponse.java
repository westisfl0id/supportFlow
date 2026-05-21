package com.supportflow.audit.dto;

import com.supportflow.audit.enums.AuditAction;
import com.supportflow.user.enums.UserRole;
import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long ticketId,
        Long actorId,
        String actorName,
        UserRole actorRole,
        AuditAction action,
        String oldValue,
        String newValue,
        String message,
        LocalDateTime createdAt
) {
}
