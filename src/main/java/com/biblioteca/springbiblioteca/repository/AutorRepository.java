package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Integer> {

	boolean existsByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);

	boolean existsByNombreIgnoreCaseAndApellidoIgnoreCaseAndIdAutorNot(String nombre, String apellido, Integer idAutor);
}
