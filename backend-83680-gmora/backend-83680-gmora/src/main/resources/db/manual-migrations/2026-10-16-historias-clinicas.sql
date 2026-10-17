-- =====================================================================
-- Migracion: historias clinicas, anotaciones y enlace formula -> historia
-- Ejecutar UNA SOLA VEZ en la base `clinica`, ANTES de arrancar el backend
-- (la entidad FormulaMedica ahora mapea la columna historia_id).
--
-- Antes de ejecutar verifique que no existan tablas con esos nombres:
--     SHOW TABLES LIKE 'historia_clinica';
--     SHOW TABLES LIKE 'anotacion';
-- Si ya existen con otras columnas, NO ejecute este script: revise su estructura
-- con DESCRIBE y ajuste las entidades HistoriaClinica / Anotacion.
-- =====================================================================

USE clinica;

-- 1) Historias clinicas (una por cita atendida: cita_id es UNIQUE)
CREATE TABLE IF NOT EXISTS historia_clinica (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    mascota_id              INT          NOT NULL,
    cita_id                 BIGINT UNSIGNED NOT NULL,
    medico_id               BIGINT UNSIGNED NOT NULL,
    fecha_hora              DATETIME     NOT NULL,
    motivo                  VARCHAR(255) NOT NULL,
    diagnostico             TEXT         NOT NULL,
    observaciones           TEXT         NULL,
    estado                  VARCHAR(20)  NOT NULL DEFAULT 'ABIERTA',
    fecha_creacion_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_historia_cita (cita_id),
    KEY idx_historia_mascota (mascota_id),
    KEY idx_historia_medico (medico_id),
    KEY idx_historia_fecha (fecha_hora)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 2) Anotaciones (siempre pertenecen a una historia)
CREATE TABLE IF NOT EXISTS anotacion (
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    historia_id           BIGINT UNSIGNED NOT NULL,
    medico_id             BIGINT UNSIGNED NOT NULL,
    fecha_hora            DATETIME     NOT NULL,
    descripcion           VARCHAR(500) NOT NULL,
    observacion           TEXT         NULL,
    diagnostico_evolucion TEXT         NOT NULL,
    PRIMARY KEY (id),
    KEY idx_anotacion_historia (historia_id, fecha_hora),
    KEY idx_anotacion_medico (medico_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 3) Relacion Historia 1:N Formula (opcional: se enlaza sola al crear la historia)
ALTER TABLE formula_medica ADD COLUMN historia_id BIGINT UNSIGNED NULL;

-- 4) Llaves foraneas (integridad referencial: no hay informacion huerfana).
--    Si alguna falla con error 3780 / 150 es porque el tipo de la columna no coincide
--    con el de la tabla referenciada: ejecute SHOW CREATE TABLE <tabla>; y ajuste el tipo
--    (por ejemplo INT vs BIGINT) en la tabla nueva.
ALTER TABLE historia_clinica
    ADD CONSTRAINT fk_historia_mascota FOREIGN KEY (mascota_id) REFERENCES mascota (mascota_id),
    ADD CONSTRAINT fk_historia_cita    FOREIGN KEY (cita_id)    REFERENCES cita (id),
    ADD CONSTRAINT fk_historia_medico  FOREIGN KEY (medico_id)  REFERENCES Medico (id);

ALTER TABLE anotacion
    ADD CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_id) REFERENCES historia_clinica (id),
    ADD CONSTRAINT fk_anotacion_medico   FOREIGN KEY (medico_id)   REFERENCES Medico (id);

ALTER TABLE formula_medica
    ADD CONSTRAINT fk_formula_historia FOREIGN KEY (historia_id) REFERENCES historia_clinica (id);
