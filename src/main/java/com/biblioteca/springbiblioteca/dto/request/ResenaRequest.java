package com.biblioteca.springbiblioteca.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record ResenaRequest(
        @NotNull(message = "El id del usuario es obligatorio")
        @Positive(message = "El id del usuario debe ser positivo")
        Integer idUsuario,

        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 dígitos")
        String isbn,

        @NotNull(message = "La calificacion es obligatoria")
        @Min(value = 1, message = "La calificacion debe estar entre 1 y 5")
        @Max(value = 5, message = "La calificacion debe estar entre 1 y 5")
        Integer calificacion,

        @NotBlank(message = "El comentario es obligatorio")
        @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
        String comentario
) {
}
