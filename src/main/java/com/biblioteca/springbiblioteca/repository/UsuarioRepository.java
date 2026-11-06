package com.biblioteca.springbiblioteca.repository;

import com.biblioteca.springbiblioteca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Integer idUsuario);
}
