package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Parámetros del listado de historias clínicas: paginación, orden y filtros.
 * Por defecto se ordenan por fecha y hora de la consulta descendente.
 */
@Data
public class HistoriaFiltroRq {

    private int page = 0;
    private int size = 10;
    private String sortBy = "fechaHora";
    private String sortDir = "desc";

    /** Texto libre: mascota, cliente, veterinario o motivo. */
    private String texto;

    private Long medicoId;

    /** ABIERTA o CERRADA. */
    private String estado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;
}
