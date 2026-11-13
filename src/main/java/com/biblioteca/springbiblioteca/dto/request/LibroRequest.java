package com.biblioteca.springbiblioteca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LibroRequest(
        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos")
        String isbn,

        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 200, message = "El titulo no puede exceder 200 caracteres")
        String titulo,

        @PastOrPresent(message = "La fecha de publicacion no puede ser futura")
        LocalDate fechaPublicacion,

        @NotNull(message = "El numero de paginas es obligatorio")
        @Positive(message = "El numero de paginas debe ser mayor que cero")
        Integer numPaginas,

        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Positive(message = "La cantidad de ejemplares debe ser mayor que cero")
        Integer cantidadEjemplares,

        @NotBlank(message = "La sintesis es obligatoria")
        @Size(max = 2000, message = "La sintesis no puede exceder 2000 caracteres")
        String sintesis,

        @NotNull(message = "El id del autor es obligatorio")
        @Positive(message = "El id del autor debe ser positivo")
        Integer idAutor,

        @NotNull(message = "El id del genero es obligatorio")
        @Positive(message = "El id del genero debe ser positivo")
        Integer idGenero
) {
}