package com.supportflow.comment.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long ticketId,
        Long createdById,
        String createdByName,
        String message,
        LocalDateTime createdAt
) {
}
