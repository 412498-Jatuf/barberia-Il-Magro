-- Precios y fotos nuevos (noviembre 2026), para una base que ya tenia los datos iniciales cargados.
-- En una base vacia no toca nada: los carga DatosIniciales con los valores nuevos.

UPDATE servicio SET precio = 13000 WHERE nombre = 'Corte';
UPDATE servicio SET precio = 5000 WHERE nombre = 'Barba';
UPDATE servicio SET precio = 15000, duracion_minutos = 30 WHERE nombre = 'Corte y barba';

-- Ya no hay corte infantil (solo se borra si no tiene turnos)
DELETE FROM servicio s
WHERE s.nombre = 'Corte infantil'
  AND NOT EXISTS (SELECT 1 FROM turno t WHERE t.servicio_id = s.id);

-- Galeria: las cuatro fotos nuevas reemplazan a las anteriores
DELETE FROM corte WHERE imagen_url IN (
    '/assets/img/corte-flequillo.jpg', '/assets/img/corte-mullet.jpg', '/assets/img/corte-texturizado.jpg',
    '/assets/img/corte-bowl.jpg', '/assets/img/corte-bob.jpg');

INSERT INTO corte (nombre, descripcion, categoria, imagen_url)
SELECT v.nombre, v.descripcion, v.categoria, v.imagen_url
FROM (VALUES
    ('Mullet ondulado', 'Ondas con volumen arriba, largo atrás y taper bajo alrededor de la oreja.', 'Mullet', '/assets/img/corte-mullet-ondulado.jpg'),
    ('Mullet con burst fade', 'Rulos con textura y un burst fade que rodea la oreja.', 'Mullet', '/assets/img/corte-mullet-burst.jpg'),
    ('Flequillo con mid fade', 'Flequillo largo hacia el costado y degradado medio.', 'Fade', '/assets/img/corte-flequillo-fade.jpg'),
    ('Mullet con barba', 'Flequillo, largo en la nuca, taper fade y barba perfilada.', 'Mullet', '/assets/img/corte-mullet-barba.jpg')
) AS v (nombre, descripcion, categoria, imagen_url)
WHERE EXISTS (SELECT 1 FROM servicio);
