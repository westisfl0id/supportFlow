package com.supportflow.exception;

public record FieldValidationError(
        String field,
        String message
) {
}
