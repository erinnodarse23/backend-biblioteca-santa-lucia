package com.biblioteca.springbiblioteca.dto.response;

public record UsuarioVencidoResponse(
        Integer idUsuario,
        String nombreCompleto,
        String email,
        Long prestamosVencidos
) {
}
