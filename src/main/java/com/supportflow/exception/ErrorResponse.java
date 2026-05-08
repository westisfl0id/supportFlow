package com.supportflow.exception;

public record ErrorResponse (
        String errorCode,
        String message
) {
}
