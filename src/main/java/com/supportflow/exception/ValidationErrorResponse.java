package com.supportflow.exception;

import lombok.Getter;
import org.springframework.validation.FieldError;
import java.util.List;

@Getter
public class ValidationErrorResponse {
    private final List<FieldValidationError> errors;

    public ValidationErrorResponse(List<FieldError> fieldErrors) {
        this.errors = fieldErrors.stream()
                .map(fe -> new FieldValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
    }
}
