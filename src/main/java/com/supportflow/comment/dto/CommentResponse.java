package com.supportflow.comment.dto;

import com.supportflow.user.enums.UserRole;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long ticketId,
        Long createdById,
        String createdByName,
        UserRole createdByRole,
        String message,
        LocalDateTime createdAt
) {
}
