package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibroRepository extends JpaRepository<Libro, String> {
@Query("SELECT COALESCE(AVG(r.calificacion), 0) FROM Resena r WHERE r.libro.isbn = :isbn")
    Double calcularPromedioCalificaciones(@Param("isbn") String isbn);

    @Query("SELECT COUNT(r) FROM Resena r WHERE r.libro.isbn = :isbn")
    Long countResenasByLibro(@Param("isbn") String isbn);
}
