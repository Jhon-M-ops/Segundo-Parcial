package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Solicitud para crear o editar una cita.
 * El estado solo se usa al crear; los cambios de estado posteriores se hacen
 * con operaciones explícitas (confirmar, atender, cancelar, reactivar).
 */
@Data
public class CitaRq {

    /** Cliente dueño de la mascota. */
    private Long clienteId;

    /** Mascota que será atendida (debe pertenecer al cliente). */
    private Integer mascotaId;

    /** Veterinario asignado. */
    private Long medicoId;

    /** Fecha de la cita (yyyy-MM-dd). */
    private LocalDate fecha;

    /** Hora de la cita (HH:mm). */
    private LocalTime hora;

    /** Motivo de la consulta. */
    private String motivo;

    /** Estado inicial (solo al crear). */
    private String estado;
}
