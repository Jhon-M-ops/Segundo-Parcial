package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Clase que representa la solicitud de un médico.
 * Se usa para crear, actualizar y eliminar médicos.
 */
@Data
public class MedicoRq {

    /**
     * Identificador único del médico.
     */
    private Long id;

    /**
     * Tipo de documento del médico.
     */
    private String tipoDocumento;

    /**
     * Número de documento del médico.
     */
    private String numeroDocumento;

    /**
     * Nombres del médico.
     */
    private String nombres;

    /**
     * Apellidos del médico.
     */
    private String apellidos;

    /**
     * Teléfono del médico.
     */
    private String telefono;

    /**
     * Registro profesional del médico.
     */
    private String registroProfesional;

    /**
     * Identificador de la especialización del médico.
     */
    private Long especializacionId;
}