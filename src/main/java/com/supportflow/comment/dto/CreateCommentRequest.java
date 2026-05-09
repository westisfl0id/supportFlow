package com.supportflow.comment.dto;

import jakarta.validation.constraints.*;

public record CreateCommentRequest(
        @NotBlank
        String message
) {
}
