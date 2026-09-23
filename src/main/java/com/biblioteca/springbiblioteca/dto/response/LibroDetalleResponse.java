package com.biblioteca.springbiblioteca.dto.response;

import java.time.LocalDate;

public record LibroDetalleResponse(
        String isbn,
        String titulo,
        LocalDate fechaPublicacion,
        Integer numPaginas,
        Integer cantidadEjemplares,
        String sintesis,
        String autorNombre,
        String autorApellido,
        String generoNombre,
        Double promedioCalificacion,
        Long totalResenas,
        Integer copiasDisponibles
) {
}
