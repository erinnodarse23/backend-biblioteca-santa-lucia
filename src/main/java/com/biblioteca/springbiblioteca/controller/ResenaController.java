package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.ResenaRequest;
import com.biblioteca.springbiblioteca.entity.Resena;
import com.biblioteca.springbiblioteca.repository.ResenaRepository;
import com.biblioteca.springbiblioteca.service.ResenaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
public class ResenaController {

    private final ResenaService resenaService;
    private final ResenaRepository resenaRepository;

    @GetMapping
    public List<Resena> listar() {
        return resenaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Resena obtener(@PathVariable @NotNull @Positive Integer id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resena registrarResena(@NotNull @Valid @RequestBody ResenaRequest request) {
        return resenaService.registrarResena(request);
    }

    @PutMapping("/{id}")
    public Resena actualizar(@PathVariable @NotNull @Positive Integer id,
                             @NotNull @Valid @RequestBody ResenaRequest request) {
        return resenaService.actualizarResena(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @Positive Integer id) {
        resenaRepository.deleteById(id);
    }
}
