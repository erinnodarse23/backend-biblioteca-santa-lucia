package com.biblioteca.springbiblioteca.service;

import com.biblioteca.springbiblioteca.dto.request.PrestamoRequest;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import com.biblioteca.springbiblioteca.entity.Usuario;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import com.biblioteca.springbiblioteca.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private static final long MAX_PRESTAMOS_ACTIVOS_POR_USUARIO = 5;
    private static final long MAX_PRESTAMOS_VENCIDOS_PERMITIDOS = 0;
    private static final long MIN_EJEMPLARES_DISPONIBLES_PARA_PRESTAR = 1;

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.idUsuario())
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + request.idUsuario()));

        Libro libro = libroRepository.findById(request.isbn())
            .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con ISBN: " + request.isbn()));

        validarMaximoPrestamosActivos(usuario.getIdUsuario());
        validarSinPrestamosVencidos(usuario.getIdUsuario());
        validarDisponibilidadEjemplares(libro);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaSalida(LocalDate.now());
        prestamo.setFechaDevolucionPrevista(LocalDate.now().plusDays(request.diasPrestamo()));
        prestamo.setFechaDevolucionReal(null);

        return prestamoRepository.save(prestamo);
    }

    private void validarMaximoPrestamosActivos(Integer idUsuario) {
        long activos = prestamoRepository.countByUsuarioIdUsuarioAndFechaDevolucionRealIsNull(idUsuario);
        if (activos >= MAX_PRESTAMOS_ACTIVOS_POR_USUARIO) {
            throw new IllegalStateException("El usuario ya tiene " + MAX_PRESTAMOS_ACTIVOS_POR_USUARIO + " préstamos activos");
        }
    }

    private void validarSinPrestamosVencidos(Integer idUsuario) {
        long vencidos = prestamoRepository.countVencidosByUsuario(idUsuario);
        if (vencidos > MAX_PRESTAMOS_VENCIDOS_PERMITIDOS) {
            throw new IllegalStateException("Usuario tiene préstamos vencidos. Regularice antes de solicitar.");
        }
    }

    private void validarDisponibilidadEjemplares(Libro libro) {
        long prestamosActivos = prestamoRepository.countByLibroIsbnAndFechaDevolucionRealIsNull(libro.getIsbn());
        long disponibles = (long) libro.getCantidadEjemplares() - prestamosActivos;
        if (disponibles < MIN_EJEMPLARES_DISPONIBLES_PARA_PRESTAR) {
            throw new IllegalStateException("No hay ejemplares disponibles de este libro");
        }
    }

    @Transactional
    public Prestamo registrarDevolucion(Integer idPrestamo) {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new EntityNotFoundException("Préstamo no encontrado con id: " + idPrestamo));
        if (prestamo.getFechaDevolucionReal() != null) {
            throw new IllegalStateException("Este préstamo ya fue devuelto");
        }
        prestamo.setFechaDevolucionReal(LocalDate.now());
        return prestamoRepository.save(prestamo);
    }
}
