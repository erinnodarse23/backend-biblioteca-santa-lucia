package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.PrestamoRequest;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import com.biblioteca.springbiblioteca.repository.PrestamoRepository;
import com.biblioteca.springbiblioteca.service.PrestamoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;
    private final PrestamoRepository prestamoRepository;

    @GetMapping
    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Prestamo obtener(@PathVariable @NotNull @Positive Integer id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Préstamo no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo registrarPrestamo(@NotNull @Valid @RequestBody PrestamoRequest request) {
        return prestamoService.registrarPrestamo(request);
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo registrarDevolucion(@PathVariable @NotNull @Positive Integer id) {
        return prestamoService.registrarDevolucion(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @Positive Integer id) {
        prestamoRepository.deleteById(id);
    }
}
