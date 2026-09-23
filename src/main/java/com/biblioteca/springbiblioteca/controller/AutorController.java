package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.AutorRequest;
import com.biblioteca.springbiblioteca.entity.Autor;
import com.biblioteca.springbiblioteca.repository.AutorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorRepository autorRepository;

    @GetMapping
    public List<Autor> listar() {
        return autorRepository.findAll();
    }

    @GetMapping("/{id}")
    public Autor obtener(@PathVariable @NotNull @Positive Integer id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Autor crear(@NotNull @Valid @RequestBody AutorRequest request) {
        String nombre = request.nombre().trim();
        String apellido = request.apellido().trim();
        if (autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase(nombre, apellido)) {
            throw new IllegalStateException("Ya existe un autor con el mismo nombre y apellido");
        }

        Autor autor = new Autor();
        autor.setNombre(nombre);
        autor.setApellido(apellido);
        autor.setFechaNacimiento(request.fechaNacimiento());
        autor.setNacionalidad(request.nacionalidad().trim());
        autor.setBiografia(request.biografia());
        return autorRepository.save(autor);
    }

    @PutMapping("/{id}")
    public Autor actualizar(@PathVariable @NotNull @Positive Integer id,
                            @NotNull @Valid @RequestBody AutorRequest request) {
        String nombre = request.nombre().trim();
        String apellido = request.apellido().trim();
        if (autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCaseAndIdAutorNot(nombre, apellido, id)) {
            throw new IllegalStateException("Ya existe un autor con el mismo nombre y apellido");
        }

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado con id: " + id));

        autor.setNombre(nombre);
        autor.setApellido(apellido);
        autor.setFechaNacimiento(request.fechaNacimiento());
        autor.setNacionalidad(request.nacionalidad().trim());
        autor.setBiografia(request.biografia());

        return autorRepository.save(autor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @Positive Integer id) {
        autorRepository.deleteById(id);
    }
}
