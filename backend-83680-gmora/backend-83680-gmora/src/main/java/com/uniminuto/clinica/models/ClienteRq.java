package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Clase que representa la solicitud de un cliente.
 * Se usa para crear, actualizar y eliminar clientes.
 */
@Data
public class ClienteRq {

    /**
     * Identificador único del cliente.
     */
    private Long clienteId;

    /**
     * Tipo de documento del cliente.
     */
    private String tipoDocumento;

    /**
     * Número de documento del cliente.
     */
    private String numeroDocumento;

    /**
     * Nombres del cliente.
     */
    private String nombres;

    /**
     * Apellidos del cliente.
     */
    private String apellidos;

    /**
     * Fecha de nacimiento del cliente.
     */
    private LocalDate fechaNacimiento;

    /**
     * Género del cliente.
     */
    private String genero;

    /**
     * Teléfono del cliente.
     */
    private String telefono;

    /**
     * Dirección del cliente.
     */
    private String direccion;

    /**
     * Indica si el cliente está activo.
     */
    private Boolean activo;
}