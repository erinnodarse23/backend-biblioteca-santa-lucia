package com.biblioteca.springbiblioteca.controller;

import com.biblioteca.springbiblioteca.dto.request.UsuarioRequest;
import com.biblioteca.springbiblioteca.dto.response.UsuarioVencidoResponse;
import com.biblioteca.springbiblioteca.dto.response.VerificacionResenaResponse;
import com.biblioteca.springbiblioteca.entity.Usuario;
import com.biblioteca.springbiblioteca.repository.UsuarioRepository;
import com.biblioteca.springbiblioteca.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable @NotNull @Positive Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@NotNull @Valid @RequestBody UsuarioRequest request) {
        String email = request.email().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Ya existe un usuario con el mismo email");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setDireccion(request.direccion().trim());
        return usuarioRepository.save(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable @NotNull @Positive Integer id,
                              @NotNull @Valid @RequestBody UsuarioRequest request) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con id: " + id);
        }

        String email = request.email().trim();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(email, id)) {
            throw new IllegalStateException("Ya existe un usuario con el mismo email");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setEmail(email);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setDireccion(request.direccion().trim());
        return usuarioRepository.save(usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable @NotNull @Positive Integer id) {
        usuarioRepository.deleteById(id);
    }

    @GetMapping("/vencidos")
    public List<UsuarioVencidoResponse> getUsuariosVencidos() {
        return usuarioService.obtenerUsuariosConVencidos();
    }

    @GetMapping("/{idUsuario}/resena/{isbn}")
    public VerificacionResenaResponse verificarResena(@PathVariable @NotNull @Positive Integer idUsuario,
                                                      @PathVariable @NotBlank
                                                      @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 digitos") String isbn) {
        boolean existe = usuarioService.existeResena(idUsuario, isbn);
        return new VerificacionResenaResponse(existe);
    }
}