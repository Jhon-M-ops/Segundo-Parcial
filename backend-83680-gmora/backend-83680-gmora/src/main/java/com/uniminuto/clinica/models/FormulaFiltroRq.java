package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Parámetros de consulta (query string) del listado de fórmulas:
 * paginación, ordenamiento y filtros. Por defecto devuelve las fórmulas
 * más recientes primero.
 */
@Data
public class FormulaFiltroRq {

    /** Número de página (base 0). */
    private int page = 0;

    /** Registros por página. */
    private int size = 10;

    /** Columna de ordenamiento (ver lista blanca en el servicio). */
    private String sortBy = "fechaCreacion";

    /** Dirección del ordenamiento: asc o desc. */
    private String sortDir = "desc";

    /** Texto libre: mascota, cliente, veterinario o medicamento. */
    private String texto;

    /** Filtro por veterinario. */
    private Long medicoId;

    /** Filtro por medicamento. */
    private Long medicamentoId;

    /** Filtro por estado (true = activa, false = inactiva). */
    private Boolean activo;

    /** Fecha de emisión desde (inclusive). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    /** Fecha de emisión hasta (inclusive). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;
}
