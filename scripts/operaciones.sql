-- DDL – Creación de una vista para disponibilidad de ejemplares

CREATE OR REPLACE VIEW disponibilidad_ejemplares AS
SELECT 
    l.isbn,
    l.titulo,
    l.cantidad_ejemplares - COUNT(p.id_prestamo) AS disponibles
FROM libro l
LEFT JOIN prestamo p ON l.isbn = p.isbn AND p.fecha_devolucion_real IS NULL
GROUP BY l.isbn, l.titulo, l.cantidad_ejemplares;


-- DML – Insertar un nuevo libro (operación CRUD)

INSERT INTO libro (isbn, titulo, fecha_publicacion, num_paginas, cantidad_ejemplares, sintesis, id_autor, id_genero)
VALUES ('9788437604947', 'Cien años de soledad', '1967-05-30', 432, 5, 'Novela del realismo mágico', 1, 2);


-- DML – Actualizar la devolución de un préstamo

UPDATE prestamo
SET fecha_devolucion_real = CURRENT_DATE
WHERE id_prestamo = 101 AND fecha_devolucion_real IS NULL;


-- DML – Eliminar una reseña (si fuera necesario)

DELETE FROM resena
WHERE id_resena = 25 AND id_usuario = 10;


-- DML – Consulta con JOIN para detalle completo de un libro 
-- (incluyendo autor, género y promedio de calificaciones)

SELECT 
    l.isbn,
    l.titulo,
    l.fecha_publicacion,
    l.num_paginas,
    l.cantidad_ejemplares,
    l.sintesis,
    a.nombre AS autor_nombre,
    a.apellido AS autor_apellido,
    g.nombre AS genero,
    COALESCE(AVG(r.calificacion), 0) AS promedio_calificacion,
    COUNT(r.id_resena) AS total_resenas,
    (SELECT disponibles FROM disponibilidad_ejemplares WHERE isbn = l.isbn) AS copias_disponibles
FROM libro l
JOIN autor a ON l.id_autor = a.id_autor
JOIN genero g ON l.id_genero = g.id_genero
LEFT JOIN resena r ON l.isbn = r.isbn
WHERE l.isbn = '9788437604947'
GROUP BY l.isbn, l.titulo, l.fecha_publicacion, l.num_paginas, l.cantidad_ejemplares, l.sintesis, a.nombre, a.apellido, g.nombre;


-- DML – Consulta de préstamos de un libro por rango de fechas

SELECT p.*, u.nombre, u.apellido
FROM prestamo p
JOIN usuario u ON p.id_usuario = u.id_usuario
WHERE p.isbn = '9788437604947'
  AND p.fecha_salida BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY p.fecha_salida;


-- DML – Listar usuarios con préstamos vencidos (y contar cuántos)

SELECT 
    u.id_usuario,
    u.nombre,
    u.apellido,
    u.email,
    COUNT(p.id_prestamo) AS prestamos_vencidos
FROM usuario u
JOIN prestamo p ON u.id_usuario = p.id_usuario
WHERE p.fecha_devolucion_prevista < CURRENT_DATE
  AND p.fecha_devolucion_real IS NULL
GROUP BY u.id_usuario, u.nombre, u.apellido, u.email
ORDER BY prestamos_vencidos DESC;


-- DML – Verificar si un usuario ya reseñó un libro (evitar duplicados)

SELECT EXISTS (
    SELECT 1 FROM resena
    WHERE id_usuario = 5 AND isbn = '9788437604947'
) AS ya_resenado;


-- DML – Subconsulta para saber qué libros no tienen reseñas

SELECT isbn, titulo
FROM libro
WHERE isbn NOT IN (SELECT DISTINCT isbn FROM resena);


-- DML – Uso de funciones de agregación: calificación promedio por género

SELECT g.nombre AS genero, AVG(r.calificacion) AS promedio
FROM genero g
JOIN libro l ON g.id_genero = l.id_genero
JOIN resena r ON l.isbn = r.isbn
GROUP BY g.id_genero, g.nombre
HAVING AVG(r.calificacion) >= 4.0;


-- DCL – Ejemplo de permisos (asumiendo roles)

-- Crear rol de bibliotecario
CREATE ROLE bibliotecario WITH LOGIN PASSWORD '1234';
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO bibliotecario;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO bibliotecario;

-- Rol de usuario lector (solo consultas y reseñas propias)
CREATE ROLE lector WITH LOGIN PASSWORD '5678';
GRANT SELECT ON libro, autor, genero, disponibilidad_ejemplares TO lector;
GRANT INSERT, UPDATE ON resena TO lector;
