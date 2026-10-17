package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que representa la solicitud de una especialización.
 * Se usa para crear, actualizar y eliminar especializaciones.
 */
@Data
public class EspecializacionRq {

    /**
     * Identificador único de la especialización.
     */
    private Long id;

    /**
     * Nombre de la especialización.
     */
    private String nombre;

    /**
     * Descripción de la especialización.
     */
    private String descripcion;

    /**
     * Código de la especialización.
     */
    private String codigoEspecializacion;
}