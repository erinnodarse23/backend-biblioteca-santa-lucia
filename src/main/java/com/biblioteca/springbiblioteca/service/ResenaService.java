package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.request.ResenaRequest;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Resena;
import com.biblioteca.springbiblioteca.entity.Usuario;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.repository.ResenaRepository;
import com.biblioteca.springbiblioteca.repository.UsuarioRepository;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ResenaService {

    private static final int MAX_RESENAS_POR_USUARIO_Y_LIBRO = 1;
    private static final int MIN_PRESTAMOS_DEVUELTOS_PARA_RESENAR = 1;

    private final ResenaRepository resenaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public ResenaService(ResenaRepository resenaRepository,
                         UsuarioRepository usuarioRepository,
                         LibroRepository libroRepository,
                         PrestamoRepository prestamoRepository) {
        this.resenaRepository = resenaRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public Resena registrarResena(ResenaRequest request) {
        validarUnaResenaPorLibro(request.idUsuario(), request.isbn());
        validarResenaSoloDespuesDeDevolucion(request.idUsuario(), request.isbn());

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.idUsuario()));
        Libro libro = libroRepository.findById(request.isbn())
            .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con ISBN: " + request.isbn()));

        Resena resena = new Resena();
        resena.setUsuario(usuario);
        resena.setLibro(libro);
        resena.setCalificacion(request.calificacion());
        resena.setComentario(request.comentario());
        resena.setFechaPublicacion(LocalDate.now());

        return resenaRepository.save(resena);
    }

    private void validarUnaResenaPorLibro(Integer idUsuario, String isbn) {
        boolean yaReseno = resenaRepository.existsByUsuarioIdUsuarioAndLibroIsbn(idUsuario, isbn);
        if (yaReseno && MAX_RESENAS_POR_USUARIO_Y_LIBRO <= 1) {
            throw new IllegalStateException("El usuario ya registró una reseña para este libro");
        }
    }

    private void validarResenaSoloDespuesDeDevolucion(Integer idUsuario, String isbn) {
        long prestamosDevueltos = prestamoRepository
                .countByUsuarioIdUsuarioAndLibroIsbnAndFechaDevolucionRealIsNotNull(idUsuario, isbn);
        if (prestamosDevueltos < MIN_PRESTAMOS_DEVUELTOS_PARA_RESENAR) {
            throw new IllegalStateException("Solo puede reseñar libros que ya ha devuelto");
        }
    }

    public Resena actualizarResena(Integer id, ResenaRequest request) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id: " + id));

        resena.setCalificacion(request.calificacion());
        resena.setComentario(request.comentario());

        return resenaRepository.save(resena);
    }
}
