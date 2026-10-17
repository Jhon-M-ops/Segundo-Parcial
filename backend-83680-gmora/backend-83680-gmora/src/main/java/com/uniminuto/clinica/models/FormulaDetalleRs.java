package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Información completa de una fórmula, usada en la vista "Ver" y para
 * precargar el formulario de edición.
 */
@Data
public class FormulaDetalleRs {

    private Long formulaId;
    private Integer mascotaId;
    private String mascota;
    private Long clienteId;
    private String cliente;
    private Long historiaClinicaId;
    private Long citaId;
    private LocalDateTime citaFechaHora;
    private String citaMotivo;
    private Long medicoId;
    private String veterinario;
    private Long medicamentoId;
    private String medicamento;
    private LocalDate fechaEmision;
    private BigDecimal dosis;
    private String unidadDosis;
    private String frecuencia;
    private Integer duracionDias;
    private String indicaciones;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
