package com.uniminuto.clinica.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Solicitud para crear o editar una fórmula médica.
 */
@Data
public class FormulaRq {

    /** Identificador de la mascota. */
    private Integer mascotaId;

    /** Identificador de la cita (consulta) asociada. */
    private Long citaId;

    /** Identificador del veterinario que emite la fórmula. */
    private Long medicoId;

    /** Identificador del medicamento formulado. */
    private Long medicamentoId;

    /** Fecha de emisión (formato ISO yyyy-MM-dd). */
    private LocalDate fechaEmision;

    /** Cantidad de la dosis. */
    private BigDecimal dosis;

    /** Unidad de la dosis (mg, ml, tableta...). */
    private String unidadDosis;

    /** Frecuencia de administración. */
    private String frecuencia;

    /** Duración del tratamiento en días. */
    private Integer duracionDias;

    /** Indicaciones adicionales. */
    private String indicaciones;
}
