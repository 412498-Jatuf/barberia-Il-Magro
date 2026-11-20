-- Cancelacion de turnos por link: cada turno tiene un codigo secreto que va en el link de cancelacion,
-- y se guarda cuando se cancelo. Los turnos anteriores a esta migracion quedan sin codigo.
ALTER TABLE turno ADD COLUMN codigo_cancelacion VARCHAR(32);
ALTER TABLE turno ADD COLUMN cancelado_en TIMESTAMP;

CREATE UNIQUE INDEX uk_turno_codigo_cancelacion ON turno (codigo_cancelacion);
