package com.biblioteca.springbiblioteca.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genero")
@Getter
@Setter
@NoArgsConstructor
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_genero")
    private Integer idGenero;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}