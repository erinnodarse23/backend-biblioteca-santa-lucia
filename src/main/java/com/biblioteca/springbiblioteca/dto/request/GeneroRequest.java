package com.biblioteca.springbiblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GeneroRequest(
        @NotBlank(message = "El nombre del genero es obligatorio")
        @Size(max = 50, message = "El nombre del genero no puede exceder 50 caracteres")
        @Pattern(regexp = "^[\\p{L}\\s]+$", message = "El nombre del genero solo puede contener letras")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
        String descripcion
) {
}