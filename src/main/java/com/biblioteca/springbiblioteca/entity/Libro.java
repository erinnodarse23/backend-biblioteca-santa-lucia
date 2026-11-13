package com.biblioteca.springbiblioteca.entity;

import jakarta.persistence.*;
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
    private String isbn;

    private String titulo;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "num_paginas")
    private Integer numPaginas;

    @Column(name = "cantidad_ejemplares")
    private Integer cantidadEjemplares;

    @Column(columnDefinition = "TEXT")
    private String sintesis;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    private Autor autor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_genero", nullable = false)
    private Genero genero;
}