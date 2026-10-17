package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Clase que representa la solicitud de un medicamento.
 * Se usa para crear, actualizar y eliminar medicamentos.
 */
@Data
public class MedicamentoRq {

    /**
     * Identificador único del medicamento.
     */
    private Long id;

    /**
     * Nombre del medicamento.
     */
    private String nombre;

    /**
     * Descripción del medicamento.
     */
    private String descripcion;

    /**
     * Presentación del medicamento.
     */
    private String presentacion;

    /**
     * Fecha de compra del medicamento.
     */
    private LocalDate fechaCompra;

    /**
     * Fecha de vencimiento del medicamento.
     */
    private LocalDate fechaVence;
}