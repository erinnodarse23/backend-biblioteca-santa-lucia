package com.biblioteca.springbiblioteca.exception;

import java.util.List;

public record ValidationErrorResponse(
        String error,
        String message,
        int status,
        String timestamp,
        List<ValidationError> invalidFields
) {
    public record ValidationError(String field, String reason) {}
}
