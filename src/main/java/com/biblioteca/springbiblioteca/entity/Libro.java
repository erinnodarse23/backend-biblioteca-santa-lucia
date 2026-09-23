package com.biblioteca.springbiblioteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "libro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Libro {

    @Id
    @Column(name = "isbn")
    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(regexp = "^[0-9]{13}$", message = "El ISBN debe tener 13 dígitos")
    private String isbn;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 200, message = "El titulo no puede exceder 200 caracteres")
    private String titulo;

    @Column(name = "fecha_publicacion")
    @PastOrPresent(message = "La fecha de publicacion no puede ser futura")
    private LocalDate fechaPublicacion;

    @Column(name = "num_paginas")
    @NotNull(message = "El numero de paginas es obligatorio")
    @Positive(message = "El numero de paginas debe ser mayor que cero")
    private Integer numPaginas;

    @Column(name = "cantidad_ejemplares")
    @NotNull(message = "La cantidad de ejemplares es obligatoria")
    @Positive(message = "La cantidad de ejemplares debe ser mayor que cero")
    private Integer cantidadEjemplares;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "La sintesis es obligatoria")
    @Size(max = 2000, message = "La sintesis no puede exceder 2000 caracteres")
    private String sintesis;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    @NotNull(message = "El autor es obligatorio")
    private Autor autor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_genero", nullable = false)
    @NotNull(message = "El genero es obligatorio")
    private Genero genero;
}
