package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.response.UsuarioVencidoResponse;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import com.biblioteca.springbiblioteca.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PrestamoRepository prestamoRepository;
    private final ResenaRepository resenaRepository;

    public List<UsuarioVencidoResponse> obtenerUsuariosConVencidos() {
        return prestamoRepository.findUsuariosConPrestamosVencidos();
    }

    public boolean existeResena(Integer idUsuario, String isbn) {
        return resenaRepository.existsByUsuarioIdUsuarioAndLibroIsbn(idUsuario, isbn);
    }
}
