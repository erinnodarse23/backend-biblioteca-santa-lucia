package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.request.PrestamoRequest;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import com.biblioteca.springbiblioteca.entity.Usuario;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import com.biblioteca.springbiblioteca.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    private static final Integer USER_ID = 1;
    private static final String ISBN = "9788437604947";

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LibroRepository libroRepository;
    @InjectMocks private PrestamoService prestamoService;

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(USER_ID);
        return u;
    }

    private Libro libro(int ejemplares) {
        Libro l = new Libro();
        l.setIsbn(ISBN);
        l.setCantidadEjemplares(ejemplares);
        return l;
    }

    private PrestamoRequest request() {
        return new PrestamoRequest(USER_ID, ISBN, 7);
    }

    @Test
    void registrarPrestamo_cuandoUsuarioNoExiste_lanzaEntityNotFound() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> prestamoService.registrarPrestamo(request()));
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void registrarPrestamo_cuandoLibroNoExiste_lanzaEntityNotFound() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario()));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> prestamoService.registrarPrestamo(request()));
    }

    @Test
    void registrarPrestamo_cuando5Activos_lanzaIllegalState() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario()));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.of(libro(10)));
        when(prestamoRepository.countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(USER_ID)).thenReturn(5L);
        assertThrows(IllegalStateException.class,
                () -> prestamoService.registrarPrestamo(request()));
        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void registrarPrestamo_cuandoTieneVencidos_lanzaIllegalState() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario()));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.of(libro(10)));
        when(prestamoRepository.countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(USER_ID)).thenReturn(2L);
        when(prestamoRepository.countVencidosByUsuario(USER_ID)).thenReturn(1L);
        assertThrows(IllegalStateException.class,
                () -> prestamoService.registrarPrestamo(request()));
    }

    @Test
    void registrarPrestamo_cuandoSinEjemplares_lanzaIllegalState() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario()));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.of(libro(2)));
        when(prestamoRepository.countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(USER_ID)).thenReturn(0L);
        when(prestamoRepository.countVencidosByUsuario(USER_ID)).thenReturn(0L);
        when(prestamoRepository.countByLibroIsbnAndFechaDevolucionRealIsNull(ISBN)).thenReturn(2L);
        assertThrows(IllegalStateException.class,
                () -> prestamoService.registrarPrestamo(request()));
    }

    @Test
    void registrarPrestamo_cuandoTodoOk_guardaPrestamo() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario()));
        when(libroRepository.findById(ISBN)).thenReturn(Optional.of(libro(3)));
        when(prestamoRepository.countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(USER_ID)).thenReturn(0L);
        when(prestamoRepository.countVencidosByUsuario(USER_ID)).thenReturn(0L);
        when(prestamoRepository.countByLibroIsbnAndFechaDevolucionRealIsNull(ISBN)).thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        Prestamo result = prestamoService.registrarPrestamo(request());

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getFechaSalida());
        assertEquals(LocalDate.now().plusDays(7), result.getFechaDevolucionPrevista());
        assertNull(result.getFechaDevolucionReal());
        verify(prestamoRepository).save(any(Prestamo.class));
    }

    @Test
    void registrarDevolucion_cuandoNoExiste_lanzaEntityNotFound() {
        when(prestamoRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> prestamoService.registrarDevolucion(999));
    }

    @Test
    void registrarDevolucion_cuandoYaDevuelto_lanzaIllegalState() {
        Prestamo p = new Prestamo();
        p.setFechaDevolucionReal(LocalDate.now().minusDays(1));
        when(prestamoRepository.findById(1)).thenReturn(Optional.of(p));
        assertThrows(IllegalStateException.class,
                () -> prestamoService.registrarDevolucion(1));
    }

    @Test
    void registrarDevolucion_cuandoActivo_marcaFecha() {
        Prestamo p = new Prestamo();
        p.setFechaDevolucionReal(null);
        when(prestamoRepository.findById(1)).thenReturn(Optional.of(p));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        Prestamo result = prestamoService.registrarDevolucion(1);

        assertNotNull(result.getFechaDevolucionReal());
        assertEquals(LocalDate.now(), result.getFechaDevolucionReal());
    }
}