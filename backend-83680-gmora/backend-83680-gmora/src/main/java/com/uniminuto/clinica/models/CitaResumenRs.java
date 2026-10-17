package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Información resumida de una cita para el listado.
 */
@Data
public class CitaResumenRs {

    private Long citaId;
    private LocalDateTime fechaHora;
    private String cliente;
    private String mascota;
    private String veterinario;
    private String estado;
}
