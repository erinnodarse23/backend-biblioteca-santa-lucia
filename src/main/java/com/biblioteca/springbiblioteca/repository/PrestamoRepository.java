package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.dto.response.UsuarioVencidoResponse;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    long countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(Integer idUsuario);

    @Query("SELECT COUNT(p) FROM Prestamo p " +
           "WHERE p.usuario.idUsuario = :idUsuario " +
           "AND p.fechaDevolucionPrevista < CURRENT_DATE " +
           "AND p.fechaDevolucionReal IS NULL")
    long countVencidosByUsuario(@Param("idUsuario") Integer idUsuario);

    long countByLibroIsbnAndFechaDevolucionRealIsNull(String isbn);

    long countByUsuarioIdUsuarioAndLibroIsbnAndFechaDevolucionRealIsNotNull(Integer idUsuario, String isbn);

    List<Prestamo> findByLibroIsbnAndFechaSalidaBetween(String isbn,
                                                         LocalDate inicio,
                                                         LocalDate fin);

    void deleteByFechaDevolucionRealBefore(LocalDate fecha);

    @Query("""
           SELECT new com.biblioteca.springbiblioteca.dto.response.UsuarioVencidoResponse(
               p.usuario.idUsuario,
               CONCAT(p.usuario.nombre, ' ', p.usuario.apellido),
               p.usuario.email,
               COUNT(p)
           )
           FROM Prestamo p
           WHERE p.fechaDevolucionPrevista < CURRENT_DATE
             AND p.fechaDevolucionReal IS NULL
           GROUP BY p.usuario.idUsuario, p.usuario.nombre, p.usuario.apellido, p.usuario.email
           ORDER BY COUNT(p) DESC
           """)
    List<UsuarioVencidoResponse> findUsuariosConPrestamosVencidos();
}
