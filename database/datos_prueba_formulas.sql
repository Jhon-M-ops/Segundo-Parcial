-- =====================================================================
-- Datos de prueba para citas, fórmulas e historias clinicas
-- (esquema real: cita.id y cita.cliente_id obligatorio)
-- Requiere mascotas y al menos un medico registrados.
-- Para el entregable "respaldo de la base de datos":
--      mysqldump -u root -p clinica > clinica_respaldo.sql
-- =====================================================================
USE clinica;

-- Citas ATENDIDAS (hace 2 dias) para las 3 primeras mascotas: sirven para fórmulas e historias.
INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, motivo, estado)
SELECT m.cliente_id, m.mascota_id, (SELECT MIN(id) FROM Medico),
       DATE_SUB(NOW(), INTERVAL 2 DAY), 'Control general', 'ATENDIDA'
FROM mascota m ORDER BY m.mascota_id LIMIT 3;

-- Una cita CANCELADA (prueba que no admite fórmulas ni historia).
INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, motivo, estado)
SELECT m.cliente_id, m.mascota_id, (SELECT MIN(id) FROM Medico),
       DATE_SUB(NOW(), INTERVAL 1 DAY), 'Vacunacion', 'CANCELADA'
FROM mascota m ORDER BY m.mascota_id LIMIT 1;

-- Una cita futura PROGRAMADA (prueba "emision >= fecha de la consulta" y "no se atiende una cita futura").
INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, motivo, estado)
SELECT m.cliente_id, m.mascota_id, (SELECT MIN(id) FROM Medico),
       DATE_ADD(NOW(), INTERVAL 5 DAY), 'Revision de heridas', 'PROGRAMADA'
FROM mascota m ORDER BY m.mascota_id LIMIT 1;
