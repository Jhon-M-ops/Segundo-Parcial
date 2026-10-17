-- =====================================================================
-- Verificacion de datos despues de las migraciones (todas deben dar 0)
-- Detecta registros antiguos que el codigo no podria mostrar sin error.
-- =====================================================================
USE clinica;

-- Formulas que la migracion 2026-10-10 no pudo completar (cita inexistente, texto sin dosis, etc.)
SELECT COUNT(*) AS formulas_incompletas
FROM formula_medica
WHERE mascota_id IS NULL OR medico_id IS NULL OR fecha_emision IS NULL
   OR unidad_dosis IS NULL OR frecuencia IS NULL OR activo IS NULL;

-- Citas con un estado que la aplicacion no reconoce
-- (validos: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, PROGRAMADA)
SELECT COUNT(*) AS citas_con_estado_desconocido
FROM cita
WHERE UPPER(estado) NOT IN ('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'CANCELADA', 'PROGRAMADA');

-- Citas cuyo cliente no coincide con el dueno de la mascota
SELECT COUNT(*) AS citas_cliente_distinto_al_de_la_mascota
FROM cita c JOIN mascota m ON m.mascota_id = c.mascota_id
WHERE c.cliente_id <> m.cliente_id;

-- Formulas cuya mascota no coincide con la mascota de su cita
SELECT COUNT(*) AS formulas_mascota_distinta_a_la_de_la_cita
FROM formula_medica f JOIN cita c ON c.id = f.cita_id
WHERE f.mascota_id <> c.mascota_id;
