package com.uniminuto.clinica.models;

import lombok.Data;

/**
 * Solicitud para agregar una anotación a una historia clínica.
 * La fecha y hora las asigna el servidor.
 */
@Data
public class AnotacionRq {

    /** Veterinario que escribe la anotación. */
    private Long medicoId;

    /** Descripción (obligatoria, máximo 500 caracteres). */
    private String descripcion;

    /** Observación (opcional, máximo 2000 caracteres). */
    private String observacion;

    /** Diagnóstico o evolución (obligatorio, máximo 2000 caracteres). */
    private String diagnosticoEvolucion;
}
