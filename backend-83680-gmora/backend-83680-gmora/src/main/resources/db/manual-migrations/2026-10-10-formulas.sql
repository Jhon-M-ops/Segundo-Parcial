-- Extend the existing formula table without deleting its legacy columns or rows.
ALTER TABLE formula_medica
    ADD COLUMN mascota_id INT NULL,
    ADD COLUMN medico_id BIGINT NULL,
    ADD COLUMN fecha_emision DATE NULL,
    ADD COLUMN dosis_numero DECIMAL(10, 2) NULL,
    ADD COLUMN unidad_dosis VARCHAR(20) NULL,
    ADD COLUMN frecuencia VARCHAR(100) NULL,
    ADD COLUMN duracion_dias INT NULL,
    ADD COLUMN activo BOOLEAN NULL;

-- Derive the new fields that are available from the existing formula and appointment data.
UPDATE formula_medica fm
JOIN cita c ON c.id = fm.cita_id
SET fm.mascota_id = c.mascota_id,
    fm.medico_id = c.medico_id,
    fm.fecha_emision = DATE(fm.fecha_creacion_registro),
    fm.dosis_numero = CASE
        WHEN LOWER(fm.dosis) REGEXP 'una[[:space:]]+pastilla|una[[:space:]]+tableta' THEN 1
        ELSE CAST(REGEXP_SUBSTR(fm.dosis, '^[[:space:]]*[0-9]+([.][0-9]+)?') AS DECIMAL(10, 2))
    END,
    fm.unidad_dosis = COALESCE(
        LOWER(REGEXP_SUBSTR(fm.dosis, '(mg|ml|mcg|g|tabletas?|pastillas?|gotas?)', 1, 1, 'i')),
        'unidad'),
    fm.frecuencia = COALESCE(
        REGEXP_SUBSTR(fm.dosis, 'cada[[:space:]]+[0-9]+[[:space:]]+horas?', 1, 1, 'i'),
        'No especificada'),
    fm.duracion_dias = CAST(
        REGEXP_SUBSTR(fm.indicaciones, '[0-9]+(?=[[:space:]]+d[ií]as?)')
        AS UNSIGNED),
    fm.activo = TRUE;
