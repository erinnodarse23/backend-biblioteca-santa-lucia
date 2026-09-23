package com.biblioteca.springbiblioteca.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record PrestamoRequest(
        @NotNull(message = "El id del usuario es obligatorio")
        @Positive(message = "El id del usuario debe ser positivo")
        Integer idUsuario,

        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 dígitos")
        String isbn,

        @NotNull(message = "Los días de préstamo son obligatorios")
        @Min(value = 1, message = "Los días de préstamo deben ser al menos 1")
        @Max(value = 30, message = "Los días de préstamo no pueden superar 30")
        Integer diasPrestamo
) {
}
