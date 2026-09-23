package com.biblioteca.springbiblioteca.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "resena", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "isbn"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Integer idResena;

    @Min(1)
    @Max(5)
    private int calificacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "isbn", nullable = false)
    private Libro libro;
}
