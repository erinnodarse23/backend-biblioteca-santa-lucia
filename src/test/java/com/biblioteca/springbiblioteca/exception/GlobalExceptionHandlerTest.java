package com.biblioteca.springbiblioteca.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_retorna404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new EntityNotFoundException("Autor no encontrado"));
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Recurso no encontrado", response.getBody().error());
        assertEquals("Autor no encontrado", response.getBody().message());
    }

    @Test
    void handleIllegalState_retorna409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalState(new IllegalStateException("Máximo 5 préstamos"));
        assertEquals(409, response.getStatusCode().value());
        assertEquals("Regla de negocio violada", response.getBody().error());
    }

    @Test
    void handleDataIntegrity_retorna409SinFiltrarDetalle() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("FK violation: constraint autor_libro_fk"));
        assertEquals(409, response.getStatusCode().value());
        assertFalse(response.getBody().message().contains("FK violation"));
        assertFalse(response.getBody().message().contains("constraint"));
    }

    @Test
    void handleGeneric_retorna500SinFiltrarMensajeInterno() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("SQL error: no such column 'foo'"));
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error interno del servidor", response.getBody().error());
        assertFalse(response.getBody().message().contains("SQL"));
        assertFalse(response.getBody().message().contains("foo"));
    }
}