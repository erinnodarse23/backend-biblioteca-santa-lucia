-- Ajustado para PostgreSQL

-- Eliminación previa de tablas en orden de dependencias
DROP TABLE IF EXISTS Resena CASCADE;
DROP TABLE IF EXISTS Prestamo CASCADE;
DROP TABLE IF EXISTS Libro CASCADE;
DROP TABLE IF EXISTS Usuario CASCADE;
DROP TABLE IF EXISTS Genero CASCADE;
DROP TABLE IF EXISTS Autor CASCADE;

CREATE TABLE IF NOT EXISTS Autor (
    id_autor SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    nacionalidad VARCHAR(50),
    biografia TEXT
);

CREATE TABLE IF NOT EXISTS Genero (
    id_genero SERIAL PRIMARY KEY,
    nombre VARCHAR(50),
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS Libro (
    isbn VARCHAR(13) PRIMARY KEY,
    titulo VARCHAR(200),
    fecha_publicacion DATE,
    num_paginas INTEGER,
    cantidad_ejemplares INTEGER,
    sintesis TEXT,
    id_autor INTEGER NOT NULL,
    id_genero INTEGER NOT NULL,
    FOREIGN KEY (id_autor) REFERENCES Autor(id_autor),
    FOREIGN KEY (id_genero) REFERENCES Genero(id_genero)
);

CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(50),
    apellido VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    direccion VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS Prestamo (
    id_prestamo SERIAL PRIMARY KEY,
    fecha_salida DATE NOT NULL,
    fecha_devolucion_prevista DATE NOT NULL,
    fecha_devolucion_real DATE,
    id_usuario INTEGER NOT NULL,
    isbn VARCHAR(13) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (isbn) REFERENCES Libro(isbn)
);

CREATE TABLE IF NOT EXISTS Resena (
    id_resena SERIAL PRIMARY KEY,
    calificacion INTEGER NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    comentario TEXT,
    fecha_publicacion DATE,
    id_usuario INTEGER NOT NULL,
    isbn VARCHAR(13) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (isbn) REFERENCES Libro(isbn),
    UNIQUE (id_usuario, isbn)
);
