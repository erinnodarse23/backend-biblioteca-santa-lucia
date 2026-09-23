package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, String> {

    @Query("SELECT new com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse(" +
           "l.isbn, l.titulo, l.fechaPublicacion, l.numPaginas, l.cantidadEjemplares, l.sintesis, " +
           "a.nombre, a.apellido, g.nombre, " +
           "COALESCE(AVG(r.calificacion), 0), COUNT(r), " +
           "l.cantidadEjemplares - SUM(CASE WHEN p.fechaDevolucionReal IS NULL THEN 1 ELSE 0 END)) " +
           "FROM Libro l " +
           "LEFT JOIN l.autor a " +
           "LEFT JOIN l.genero g " +
           "LEFT JOIN Resena r ON r.libro = l " +
           "LEFT JOIN Prestamo p ON p.libro = l " +
           "WHERE l.isbn = :isbn " +
           "GROUP BY l.isbn, l.titulo, l.fechaPublicacion, l.numPaginas, l.cantidadEjemplares, l.sintesis, " +
            "a.nombre, a.apellido, g.nombre")
        Optional<LibroDetalleResponse> findDetalleCompleto(@Param("isbn") String isbn);

    @Query("SELECT COALESCE(AVG(r.calificacion), 0) FROM Resena r WHERE r.libro.isbn = :isbn")
    Double calcularPromedioCalificaciones(@Param("isbn") String isbn);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.libro.isbn = :isbn")
    Long countResenasByLibro(@Param("isbn") String isbn);
}
