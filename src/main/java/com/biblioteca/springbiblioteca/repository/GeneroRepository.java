package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Integer> {

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdGeneroNot(String nombre, Integer idGenero);
}
