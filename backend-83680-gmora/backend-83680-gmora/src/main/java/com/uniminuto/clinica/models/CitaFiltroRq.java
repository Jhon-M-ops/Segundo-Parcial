package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Parámetros de consulta del listado de citas: paginación, orden y filtros.
 * Por defecto las citas se ordenan por fecha y hora descendente.
 */
@Data
public class CitaFiltroRq {

    /** Número de página (base 0). */
    private int page = 0;

    /** Registros por página. */
    private int size = 10;

    /** Columna de ordenamiento (ver lista blanca en el servicio). */
    private String sortBy = "fechaHora";

    /** Dirección: asc o desc. */
    private String sortDir = "desc";

    /** Texto libre: mascota, cliente, veterinario o motivo. */
    private String texto;

    /** Filtro por veterinario. */
    private Long medicoId;

    /** Filtro por estado. */
    private String estado;

    /** Citas desde esta fecha (inclusive). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    /** Citas hasta esta fecha (inclusive). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;
}
