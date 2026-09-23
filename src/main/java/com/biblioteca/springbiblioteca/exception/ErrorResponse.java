package com.biblioteca.springbiblioteca.exception;

public record ErrorResponse(
        String error,
        String message,
        int status,
        String timestamp
) {
}
