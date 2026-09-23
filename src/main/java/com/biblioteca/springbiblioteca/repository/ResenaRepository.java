package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    boolean existsByUsuarioIdUsuarioAndLibroIsbn(Integer idUsuario, String isbn);

    long countByLibroIsbn(String isbn);

    void deleteByLibroIsbn(String isbn);
}
