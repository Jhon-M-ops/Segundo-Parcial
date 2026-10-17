package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Anotación de una historia clínica.
 */
@Data
public class AnotacionRs {

    private Long anotacionId;
    private Long historiaId;
    private LocalDateTime fechaHora;
    private Long medicoId;
    private String veterinario;
    private String descripcion;
    private String observacion;
    private String diagnosticoEvolucion;
}
