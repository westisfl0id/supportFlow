package com.supportflow.comment.dto;

import jakarta.validation.constraints.*;

public record CreateCommentRequest(
        @NotNull
        Long ticketId,
        @NotNull
        @Positive
        Long userId,
        @NotBlank
        String message
) {
}
