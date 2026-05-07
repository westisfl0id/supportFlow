package com.supportflow.comment.dto;

public record CreateCommentRequest(
        Long ticketId,
        Long userId,
        String message
) {
}
