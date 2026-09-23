# Biblioteca Santa Lucía

Sistema backend desarrollado como trabajo de curso para la asignatura de Bases de Datos. El proyecto gestiona una biblioteca mediante una API REST construida con Spring Boot y PostgreSQL, cubriendo el mantenimiento de autores, géneros, libros, usuarios, préstamos y reseñas.

Autor: Erin Nodarse Gonzalez  
Carrera: Ingeniería Informática  
Curso: 2do año, periodo 2024-2025

> Este repositorio contiene únicamente el backend. No incluye frontend porque el alcance del proyecto se limita a la capa de servidor y base de datos.

## Descripción del proyecto

La aplicación expone una API REST para administrar la información principal de una biblioteca académica. Permite registrar y consultar entidades, validar relaciones entre tablas y ejecutar operaciones típicas de gestión bibliotecaria, como el alta de libros, el control de préstamos, la devolución de ejemplares y la publicación de reseñas.

Además, el repositorio incluye scripts SQL para crear la estructura de la base de datos y ejemplos de consultas, vistas y operaciones de apoyo.

## Tecnologías utilizadas

| Tecnología | Versión / uso |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Web | API REST |
| Spring Data JPA | Persistencia |
| Spring Validation | Validación de datos |
| PostgreSQL | Base de datos relacional |
| Lombok | Reducción de código repetitivo |
| Maven | Gestión del proyecto |

## Estructura del proyecto

```text
Proyecto/
├── pom.xml
├── README.md
├── lib/
│   └── postgresql-42.7.7.jar
├── scripts/
│   ├── tablas.sql
│   └── operaciones.sql
└── src/
    └── main/
        ├── java/com/biblioteca/springbiblioteca/
        │   ├── controller/
        │   ├── dto/
        │   ├── entity/
        │   ├── exception/
        │   ├── mapper/
        │   ├── repository/
        │   └── service/
        └── resources/
            ├── application.properties
            └── db.properties
```

## Módulos del backend

- Autores
- Géneros
- Libros
- Usuarios
- Préstamos
- Reseñas

## Funcionalidades principales

- CRUD completo de autores, géneros, libros, usuarios, préstamos y reseñas.
- Consulta del detalle completo de un libro.
- Consulta de préstamos de un libro por rango de fechas.
- Registro de devoluciones de préstamos.
- Verificación de si un usuario ya reseñó un libro.
- Consulta de usuarios con préstamos vencidos.
- Validaciones de integridad y reglas de negocio desde la capa backend.

## Base de datos

La base de datos está modelada en PostgreSQL y la estructura principal se crea desde [scripts/tablas.sql](scripts/tablas.sql).

### Tablas principales

- `Autor`
- `Genero`
- `Libro`
- `Usuario`
- `Prestamo`
- `Resena`

### Archivo de operaciones

El archivo [scripts/operaciones.sql](scripts/operaciones.sql) contiene ejemplos de consultas, vistas, inserciones, actualizaciones, eliminaciones y ejemplos de control de acceso sobre la base de datos.

## Requisitos previos

- Java 17 o superior
- Maven 3.8+ o superior
- PostgreSQL 14 o superior
- Git

## Configuración de la base de datos

1. Crear la base de datos en PostgreSQL.

```sql
CREATE DATABASE biblioteca_santa_lucia;
```

2. Ejecutar el script de estructura.

```bash
psql -U postgres -d biblioteca_santa_lucia -f scripts/tablas.sql
```

3. Si deseas usar las consultas de ejemplo o validar la lógica SQL, ejecuta también:

```bash
psql -U postgres -d biblioteca_santa_lucia -f scripts/operaciones.sql
```

## Configuración de la aplicación

El proyecto usa `DB_PASSWORD` como variable de entorno para la contraseña de PostgreSQL.

En Windows PowerShell:

```powershell
$env:DB_PASSWORD="tu_contraseña"
```

En Linux o macOS:

```bash
export DB_PASSWORD="tu_contraseña"
```

## Instalación y ejecución

1. Clonar el repositorio.

```bash
git clone <url-del-repositorio>
cd Proyecto
```

2. Compilar y ejecutar la aplicación.

```bash
mvn spring-boot:run
```

La aplicación quedará disponible en:

```text
http://localhost:8080
```

## Endpoints principales

### Autores

- `GET /api/autores`
- `GET /api/autores/{id}`
- `POST /api/autores`
- `PUT /api/autores/{id}`
- `DELETE /api/autores/{id}`

### Géneros

- `GET /api/generos`
- `GET /api/generos/{id}`
- `POST /api/generos`
- `PUT /api/generos/{id}`
- `DELETE /api/generos/{id}`

### Libros

- `GET /api/libros`
- `GET /api/libros/{isbn}`
- `POST /api/libros`
- `PUT /api/libros/{isbn}`
- `DELETE /api/libros/{isbn}`
- `GET /api/libros/{isbn}/detalle`
- `GET /api/libros/{isbn}/prestamos?fechaIni=YYYY-MM-DD&fechaFin=YYYY-MM-DD`

### Usuarios

- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`
- `GET /api/usuarios/vencidos`
- `GET /api/usuarios/{idUsuario}/resena/{isbn}`

### Préstamos

- `GET /api/prestamos`
- `GET /api/prestamos/{id}`
- `POST /api/prestamos`
- `PUT /api/prestamos/{id}/devolucion`
- `DELETE /api/prestamos/{id}`

### Reseñas

- `GET /api/resenas`
- `GET /api/resenas/{id}`
- `POST /api/resenas`
- `PUT /api/resenas/{id}`
- `DELETE /api/resenas/{id}`

## Observaciones técnicas

- El proyecto usa validaciones en los controladores y en los DTOs para garantizar integridad de datos.
- El ISBN de los libros se maneja como una cadena de 13 dígitos.
- Las relaciones entre tablas están definidas en PostgreSQL mediante claves foráneas.
- El diseño del backend está orientado a una arquitectura simple de controladores, servicios, repositorios y entidades.

## Problemas comunes

### Error de conexión con PostgreSQL

Verifica que el servicio de PostgreSQL esté activo, que la base de datos exista y que `DB_PASSWORD` tenga el valor correcto.

### Error por variable `DB_PASSWORD` no definida

Antes de ejecutar la aplicación, asegúrate de definir la variable de entorno en la terminal donde correrás Maven.

### Error al crear un registro duplicado

El sistema valida duplicados en autores, géneros, usuarios, libros y reseñas para evitar inconsistencias en la base de datos.

