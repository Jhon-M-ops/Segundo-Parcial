package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Información resumida de una historia clínica para el listado.
 */
@Data
public class HistoriaResumenRs {

    private Long historiaId;
    private LocalDateTime fechaHora;
    private String mascota;
    private String cliente;
    private String veterinario;
    private String motivo;
    private String estado;
}
