package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Solicitud de cambio de estado de una cita.
 */
@Data
public class CitaEstadoRq {

    /** Estado destino (PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA o PROGRAMADA). */
    private String estado;
}
