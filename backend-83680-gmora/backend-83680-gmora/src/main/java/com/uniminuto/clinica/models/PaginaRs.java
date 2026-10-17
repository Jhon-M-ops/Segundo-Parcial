package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Respuesta genérica paginada. Evita exponer directamente el objeto
 * {@code Page} de Spring, cuyo JSON no es estable.
 *
 * @param <T> tipo de los elementos de la página.
 */
@Data
@AllArgsConstructor
public class PaginaRs<T> {

    /** Elementos de la página actual. */
    private List<T> content;

    /** Número de la página actual (base 0). */
    private int page;

    /** Tamaño de página solicitado. */
    private int size;

    /** Total de registros que cumplen el filtro. */
    private long totalElements;

    /** Total de páginas disponibles. */
    private int totalPages;
}
