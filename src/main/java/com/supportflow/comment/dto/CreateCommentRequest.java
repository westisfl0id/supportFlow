package com.supportflow.comment.dto;

import jakarta.validation.constraints.*;

public record CreateCommentRequest(
        @NotBlank(message = "Комментарий не может быть пустым")
        @Size(max = 3000, message = "Комментарий не должен быть длиннее 3000 символов")
        String message
) {
}
