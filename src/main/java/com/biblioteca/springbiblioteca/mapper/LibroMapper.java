package com.biblioteca.springbiblioteca.mapper;

import com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse;
import com.biblioteca.springbiblioteca.entity.Libro;
import org.springframework.stereotype.Component;

@Component
public class LibroMapper {

    public LibroDetalleResponse toDetalleResponse(Libro libro,
                                                  Double promedio,
                                                  Long totalResenas,
                                                  Integer copiasDisponibles) {
        return new LibroDetalleResponse(
                libro.getIsbn(),
                libro.getTitulo(),
                libro.getFechaPublicacion(),
                libro.getNumPaginas(),
                libro.getCantidadEjemplares(),
                libro.getSintesis(),
                libro.getAutor().getNombre(),
                libro.getAutor().getApellido(),
                libro.getGenero().getNombre(),
                promedio,
                totalResenas,
                copiasDisponibles
        );
    }
}
