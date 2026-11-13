package com.biblioteca.springbiblioteca.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        @Pattern(regexp = "^[\\p{L}\\s]+$", message = "El nombre solo puede contener letras")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
        @Pattern(regexp = "^[\\p{L}\\s]+$", message = "El apellido solo puede contener letras")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 200, message = "La direccion no puede exceder 200 caracteres")
        String direccion
) {
}