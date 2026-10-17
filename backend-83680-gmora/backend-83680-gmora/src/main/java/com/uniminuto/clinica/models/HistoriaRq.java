package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Solicitud para crear una historia clínica.
 */
@Data
public class HistoriaRq {

    /** Cliente dueño de la mascota. */
    private Long clienteId;

    /** Mascota (debe pertenecer al cliente). */
    private Integer mascotaId;

    /** Cita atendida que origina la historia. */
    private Long citaId;

    /** Veterinario que realizó la consulta. */
    private Long medicoId;

    /** Fecha y hora de la consulta (formato ISO yyyy-MM-ddTHH:mm). */
    private LocalDateTime fechaHora;

    /** Motivo de la consulta. */
    private String motivo;

    /** Diagnóstico. */
    private String diagnostico;

    /** Observaciones (opcional). */
    private String observaciones;
}
