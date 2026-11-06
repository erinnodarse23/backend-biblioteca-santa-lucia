package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.request.ResenaRequest;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Resena;
import com.biblioteca.springbiblioteca.entity.Usuario;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import com.biblioteca.springbiblioteca.repository.ResenaRepository;
import com.biblioteca.springbiblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    private static final Integer USER_ID = 1;
    private static final String ISBN = "9788437604947";

    @Mock private ResenaRepository resenaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LibroRepository libroRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @InjectMocks private ResenaService resenaService;

    private ResenaRequest request() {
        return new ResenaRequest(USER_ID, ISBN, 5, "Excelente libro");
    }

    @Test
    void registrarResena_cuandoYaResenado_lanzaIllegalState() {
        when(resenaRepository.existsByUsuarioIdUsuarioAndLibroIsbn(USER_ID, ISBN)).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> resenaService.registrarResena(request()));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void registrarResena_cuandoNoHaDevuelto_lanzaIllegalState() {
        when(resenaRepository.existsByUsuarioIdUsuarioAndLibroIsbn(USER_ID, ISBN)).thenReturn(false);
        when(prestamoRepository.countByUsuarioIdUsuarioAndLibroIsbnAndFechaDevolucionRealIsNotNull(USER_ID, ISBN))
                .thenReturn(0L);
        assertThrows(IllegalStateException.class,
                () -> resenaService.registrarResena(request()));
    }

    @Test
    void registrarResena_cuandoTodoOk_guardaResena() {
        when(resenaRepository.existsByUsuarioIdUsuarioAndLibroIsbn(USER_ID, ISBN)).thenReturn(false);
        when(prestamoRepository.countByUsuarioIdUsuarioAndLibroIsbnAndFechaDevolucionRealIsNotNull(USER_ID, ISBN))
                .thenReturn(1L);

        Usuario u = new Usuario();
        u.setIdUsuario(USER_ID);
        Libro l = new Libro();
        l.setIsbn(ISBN);
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.of(l));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(i -> i.getArgument(0));

        Resena result = resenaService.registrarResena(request());

        assertNotNull(result);
        assertEquals(5, result.getCalificacion());
        assertEquals("Excelente libro", result.getComentario());
        assertNotNull(result.getFechaPublicacion());
        verify(resenaRepository).save(any(Resena.class));
    }
}