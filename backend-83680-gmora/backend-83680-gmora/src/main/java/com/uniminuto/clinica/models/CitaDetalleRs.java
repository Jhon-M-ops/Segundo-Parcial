package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Información completa de una cita (vista "Ver" y precarga de la edición).
 */
@Data
public class CitaDetalleRs {

    private Long citaId;
    private Long clienteId;
    private String cliente;
    private Integer mascotaId;
    private String mascota;
    private Long medicoId;
    private String veterinario;
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
}
