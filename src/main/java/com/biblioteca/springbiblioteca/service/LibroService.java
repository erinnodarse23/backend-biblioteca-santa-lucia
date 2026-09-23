package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import com.biblioteca.springbiblioteca.mapper.LibroMapper;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final LibroMapper libroMapper;

    public LibroDetalleResponse obtenerDetalleCompleto(String isbn) {
        Libro libro = libroRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con ISBN: " + isbn));

        Double promedio = libroRepository.calcularPromedioCalificaciones(isbn);
        Long totalResenas = libroRepository.countResenasByLibro(isbn);

        long prestados = prestamoRepository.countByLibroIsbnAndFechaDevolucionRealIsNull(isbn);
        int disponibles = libro.getCantidadEjemplares() - (int) prestados;

        return libroMapper.toDetalleResponse(libro, promedio, totalResenas, disponibles);
    }

    public List<Prestamo> obtenerPrestamosPorRango(String isbn, LocalDate inicio, LocalDate fin) {
        if (!libroRepository.existsById(isbn)) {
            throw new EntityNotFoundException("Libro no encontrado con ISBN: " + isbn);
        }
        return prestamoRepository.findByLibroIsbnAndFechaSalidaBetween(isbn, inicio, fin);
    }
}
