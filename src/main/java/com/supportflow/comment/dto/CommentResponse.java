package com.supportflow.comment.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String message,
        String authorName,
        LocalDateTime createdAt
) {
}
