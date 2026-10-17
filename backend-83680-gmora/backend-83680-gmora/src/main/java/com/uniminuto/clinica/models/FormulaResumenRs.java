package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Información resumida de una fórmula, usada únicamente en el listado.
 */
@Data
public class FormulaResumenRs {

    /** Identificador de la fórmula. */
    private Long formulaId;

    /** Fecha y hora de creación del registro. */
    private LocalDateTime fechaCreacion;

    /** Fecha de emisión. */
    private LocalDate fechaEmision;

    /** Nombre de la mascota. */
    private String mascota;

    /** Nombre completo del cliente propietario. */
    private String cliente;

    /** Nombre completo del veterinario. */
    private String veterinario;

    /** Nombre del medicamento. */
    private String medicamento;

    /** Estado de la fórmula. */
    private Boolean activo;
}
