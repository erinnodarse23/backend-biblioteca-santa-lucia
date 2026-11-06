package com.biblioteca.springbiblioteca.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private String now() {
        return LocalDateTime.now().toString();
    }

    // ---------- 400: validación de body ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.ValidationError> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());
        return badRequest(errors);
    }

    private ValidationErrorResponse.ValidationError toValidationError(FieldError fieldError) {
        return new ValidationErrorResponse.ValidationError(
                fieldError.getField(),
                defaultMessage(fieldError.getDefaultMessage())
        );
    }

    // ---------- 400: validación de @PathVariable / @RequestParam ----------
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getAllValidationResults()) {
            if (result instanceof Errors beanErrors && beanErrors.hasFieldErrors()) {
                for (FieldError fe : beanErrors.getFieldErrors()) {
                    errors.add(new ValidationErrorResponse.ValidationError(
                            fe.getField(), defaultMessage(fe.getDefaultMessage())));
                }
                continue;
            }
            String field = result.getMethodParameter().getParameterName();
            String safeField = (field != null && !field.isBlank()) ? field : "parametro";
            for (MessageSourceResolvable r : result.getResolvableErrors()) {
                errors.add(new ValidationErrorResponse.ValidationError(
                        safeField, defaultMessage(r.getDefaultMessage())));
            }
        }
        return badRequest(errors);
    }

    // ---------- 400: JSON malformado o tipo incorrecto ----------
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = "dato";
            if (!invalidFormat.getPath().isEmpty() && invalidFormat.getPath().get(0).getFieldName() != null) {
                field = invalidFormat.getPath().get(0).getFieldName();
            }
            String reason = "El formato del dato es inválido";
            if (LocalDate.class.equals(invalidFormat.getTargetType())) {
                reason = "La fecha debe tener formato yyyy-MM-dd";
            }
            errors.add(new ValidationErrorResponse.ValidationError(field, reason));
        } else {
            errors.add(new ValidationErrorResponse.ValidationError("body", "JSON inválido o mal formado"));
        }
        return badRequest(errors);
    }

    // ---------- 400: tipo de @PathVariable / @RequestParam incorrecto ----------
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = (ex.getName() == null || ex.getName().isBlank()) ? "parametro" : ex.getName();
        String reason = "Tipo de dato inválido";
        if (LocalDate.class.equals(ex.getRequiredType())) {
            reason = "La fecha debe tener formato yyyy-MM-dd";
        }
        return badRequest(List.of(new ValidationErrorResponse.ValidationError(field, reason)));
    }

    // ---------- 404: recurso no encontrado ----------
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    // ---------- 409: regla de negocio violada ----------
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.CONFLICT, "Regla de negocio violada", ex.getMessage());
    }

    // ---------- 409: integridad referencial (FK, unique) ----------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolation: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT,
                "Violación de integridad de datos",
                "La operación viola una restricción de la base de datos (referencia o duplicado).");
    }

    // ---------- 500: no filtrar detalles internos ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error no controlado", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ocurrió un error inesperado. Contacte al administrador.");
    }

    // ---------- helpers ----------
    private ResponseEntity<ValidationErrorResponse> badRequest(List<ValidationErrorResponse.ValidationError> errors) {
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                "Error de validación",
                "Los datos enviados no son válidos",
                HttpStatus.BAD_REQUEST.value(),
                now(),
                errors
        ));
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String type, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(type, message, status.value(), now()));
    }

    private String defaultMessage(String message) {
        return (message == null || message.isBlank()) ? "Valor inválido" : message;
    }
}
