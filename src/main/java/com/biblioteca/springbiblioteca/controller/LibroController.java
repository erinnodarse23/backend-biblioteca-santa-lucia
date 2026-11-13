package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.LibroRequest;
import com.biblioteca.springbiblioteca.dto.response.LibroDetalleResponse;
import com.biblioteca.springbiblioteca.entity.Autor;
import com.biblioteca.springbiblioteca.entity.Genero;
import com.biblioteca.springbiblioteca.entity.Libro;
import com.biblioteca.springbiblioteca.entity.Prestamo;
import com.biblioteca.springbiblioteca.repository.AutorRepository;
import com.biblioteca.springbiblioteca.repository.GeneroRepository;
import com.biblioteca.springbiblioteca.repository.LibroRepository;
import com.biblioteca.springbiblioteca.service.LibroService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final GeneroRepository generoRepository;

    @GetMapping
    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    @GetMapping("/{isbn}")
    public Libro obtener(@PathVariable @NotNull @NotBlank
                         @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn) {
        return libroRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Libro no encontrado con ISBN: " + isbn));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@NotNull @Valid @RequestBody LibroRequest request) {
        if (libroRepository.existsById(request.isbn())) {
            throw new IllegalStateException("Ya existe un libro con el mismo ISBN");
        }
        Libro libro = buildLibro(request.isbn(), request);
        return libroRepository.save(libro);
    }

    @PutMapping("/{isbn}")
    public Libro actualizar(@PathVariable @NotNull @NotBlank
                            @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn,
                            @NotNull @Valid @RequestBody LibroRequest request) {
        if (!libroRepository.existsById(isbn)) {
            throw new EntityNotFoundException("Libro no encontrado con ISBN: " + isbn);
        }
        Libro libro = buildLibro(isbn, request);
        return libroRepository.save(libro);
    }

    private Libro buildLibro(String isbn, LibroRequest request) {
        Autor autor = autorRepository.findById(request.idAutor())
                .orElseThrow(() -> new EntityNotFoundException("Autor no encontrado con id: " + request.idAutor()));
        Genero genero = generoRepository.findById(request.idGenero())
                .orElseThrow(() -> new EntityNotFoundException("Genero no encontrado con id: " + request.idGenero()));

        Libro libro = new Libro();
        libro.setIsbn(isbn);
        libro.setTitulo(request.titulo().trim());
        libro.setFechaPublicacion(request.fechaPublicacion());
        libro.setNumPaginas(request.numPaginas());
        libro.setCantidadEjemplares(request.cantidadEjemplares());
        libro.setSintesis(request.sintesis().trim());
        libro.setAutor(autor);
        libro.setGenero(genero);
        return libro;
    }

    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @NotBlank
                         @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn) {
        libroRepository.deleteById(isbn);
    }

    @GetMapping("/{isbn}/detalle")
    public LibroDetalleResponse getDetalleLibro(@PathVariable @NotNull @NotBlank
                                                @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn) {
        return libroService.obtenerDetalleCompleto(isbn);
    }

    @GetMapping("/{isbn}/prestamos")
    public List<Prestamo> getPrestamosPorRango(
            @PathVariable @NotNull @NotBlank
            @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIni,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return libroService.obtenerPrestamosPorRango(isbn, fechaIni, fechaFin);
    }
}