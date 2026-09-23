package com.biblioteca.springbiblioteca.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String now() {
        return LocalDateTime.now().toString();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.ValidationError> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());

        ValidationErrorResponse body = new ValidationErrorResponse(
            "Error de validación",
            "Los datos enviados no son válidos",
            HttpStatus.BAD_REQUEST.value(),
            now(),
            errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private ValidationErrorResponse.ValidationError toValidationError(FieldError fieldError) {
        String field = fieldError.getField();
        String reason = fieldError.getDefaultMessage();
        return new ValidationErrorResponse.ValidationError(field, reason);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ValidationErrorResponse.ValidationError> errors = new ArrayList<>();

        for (ParameterValidationResult result : ex.getAllValidationResults()) {
            if (result instanceof Errors beanErrors && beanErrors.hasFieldErrors()) {
                for (FieldError fieldError : beanErrors.getFieldErrors()) {
                    errors.add(new ValidationErrorResponse.ValidationError(
                            fieldError.getField(),
                            defaultMessage(fieldError.getDefaultMessage())));
                }
                continue;
            }

            String field = result.getMethodParameter().getParameterName();
            String safeField = (field != null && !field.isBlank()) ? field : "parametro";
            for (MessageSourceResolvable resolvable : result.getResolvableErrors()) {
                errors.add(new ValidationErrorResponse.ValidationError(
                        safeField,
                        defaultMessage(resolvable.getDefaultMessage())));
            }
        }

        ValidationErrorResponse body = new ValidationErrorResponse(
                "Error de validación",
                "Los datos enviados no son válidos",
                ex.getStatusCode().value(),
                now(),
                errors
        );

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    private String defaultMessage(String message) {
        return (message == null || message.isBlank()) ? "Valor inválido" : message;
    }

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

        ValidationErrorResponse body = new ValidationErrorResponse(
                "Error de validación",
                "Los datos enviados no son válidos",
                HttpStatus.BAD_REQUEST.value(),
                now(),
                errors
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String field = (ex.getName() == null || ex.getName().isBlank()) ? "parametro" : ex.getName();
        String reason = "Tipo de dato inválido";
        if (LocalDate.class.equals(ex.getRequiredType())) {
            reason = "La fecha debe tener formato yyyy-MM-dd";
        }

        ValidationErrorResponse body = new ValidationErrorResponse(
                "Error de validación",
                "Los datos enviados no son válidos",
                HttpStatus.BAD_REQUEST.value(),
                now(),
                List.of(new ValidationErrorResponse.ValidationError(field, reason))
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Regla de negocio violada
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse body = new ErrorResponse(
                "Regla de negocio violada",
                ex.getMessage(),
                status.value(),
                now()
        );
        return new ResponseEntity<>(body, status);
    }

    // Recurso no encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse body = new ErrorResponse(
                "Recurso no encontrado",
                ex.getMessage(),
                status.value(),
                now()
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = new ErrorResponse(
                "Error interno del servidor",
                ex.getMessage(),
                status.value(),
                now()
        );
        return new ResponseEntity<>(body, status);
    }
}
