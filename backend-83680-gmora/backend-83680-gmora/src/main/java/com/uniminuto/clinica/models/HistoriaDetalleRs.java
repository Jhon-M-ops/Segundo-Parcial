package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Información completa de una historia clínica (vista "Ver").
 */
@Data
public class HistoriaDetalleRs {

    private Long historiaId;
    private Long clienteId;
    private String cliente;
    private Integer mascotaId;
    private String mascota;
    private Long citaId;
    private Long medicoId;
    private String veterinario;
    private LocalDateTime fechaHora;
    private String motivo;
    private String diagnostico;
    private String observaciones;
    private String estado;
    private LocalDateTime fechaCreacion;
}
