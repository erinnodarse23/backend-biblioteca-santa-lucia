package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.GeneroRequest;
import com.biblioteca.springbiblioteca.entity.Genero;
import com.biblioteca.springbiblioteca.repository.GeneroRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generos")
@RequiredArgsConstructor
public class GeneroController {

    private final GeneroRepository generoRepository;

    @GetMapping
    public List<Genero> listar() {
        return generoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Genero obtener(@PathVariable @NotNull @Positive Integer id) {
        return generoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genero no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Genero crear(@NotNull @Valid @RequestBody GeneroRequest request) {
        String nombre = request.nombre().trim();
        if (generoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalStateException("Ya existe un genero con el mismo nombre");
        }

        Genero genero = new Genero();
        genero.setNombre(nombre);
        if (request.descripcion() != null) {
            genero.setDescripcion(request.descripcion().trim());
        }
        return generoRepository.save(genero);
    }

    @PutMapping("/{id}")
    public Genero actualizar(@PathVariable @NotNull @Positive Integer id,
                             @NotNull @Valid @RequestBody GeneroRequest request) {
        String nombre = request.nombre().trim();
        if (generoRepository.existsByNombreIgnoreCaseAndIdGeneroNot(nombre, id)) {
            throw new IllegalStateException("Ya existe un genero con el mismo nombre");
        }

        Genero genero = generoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genero no encontrado con id: " + id));
        genero.setNombre(nombre);
        if (request.descripcion() != null) {
            genero.setDescripcion(request.descripcion().trim());
        }
        return generoRepository.save(genero);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @Positive Integer id) {
        generoRepository.deleteById(id);
    }
}