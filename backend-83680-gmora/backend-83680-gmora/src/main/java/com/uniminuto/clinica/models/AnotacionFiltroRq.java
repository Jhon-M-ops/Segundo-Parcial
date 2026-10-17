package com.uniminuto.clinica.models;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Parámetros del histórico de anotaciones: paginación, orden y filtros.
 * Por defecto desde la más reciente hasta la más antigua.
 */
@Data
public class AnotacionFiltroRq {

    private int page = 0;
    private int size = 5;

    /** Dirección del orden por fecha: desc (por defecto) o asc. */
    private String sortDir = "desc";

    /** Texto libre en descripción, observación, diagnóstico/evolución o veterinario. */
    private String texto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;
}
