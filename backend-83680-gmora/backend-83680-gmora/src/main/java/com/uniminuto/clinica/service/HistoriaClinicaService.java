package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionFiltroRq;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaDetalleRs;
import com.uniminuto.clinica.models.HistoriaFiltroRq;
import com.uniminuto.clinica.models.HistoriaResumenRs;
import com.uniminuto.clinica.models.HistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

/**
 * Casos de uso de historias clínicas y sus anotaciones.
 */
public interface HistoriaClinicaService {

    /**
     * Lista las historias con paginación, filtros y ordenamiento.
     *
     * @param filtro parámetros de consulta.
     * @return página con la información resumida de cada historia.
     */
    PaginaRs<HistoriaResumenRs> listarHistorias(HistoriaFiltroRq filtro);

    /**
     * Consulta el detalle completo de una historia.
     *
     * @param historiaId identificador de la historia.
     * @return detalle de la historia.
     */
    HistoriaDetalleRs consultarHistoria(Long historiaId);

    /**
     * Crea una historia clínica a partir de una cita atendida, validando las reglas de negocio.
     *
     * @param historiaRq datos de la historia.
     * @return respuesta de éxito.
     */
    MiRespuestaRS guardarHistoria(HistoriaRq historiaRq);

    /**
     * Cierra una historia abierta; una historia cerrada ya no admite anotaciones.
     *
     * @param historiaId identificador de la historia.
     * @return respuesta de éxito.
     */
    MiRespuestaRS cerrarHistoria(Long historiaId);

    /**
     * Lista el histórico de anotaciones de una historia (paginado, filtrado, por fecha).
     *
     * @param historiaId identificador de la historia.
     * @param filtro     parámetros de consulta.
     * @return página de anotaciones.
     */
    PaginaRs<AnotacionRs> listarAnotaciones(Long historiaId, AnotacionFiltroRq filtro);

    /**
     * Consulta el detalle de una anotación de una historia.
     *
     * @param historiaId  identificador de la historia.
     * @param anotacionId identificador de la anotación.
     * @return la anotación.
     */
    AnotacionRs consultarAnotacion(Long historiaId, Long anotacionId);

    /**
     * Agrega una anotación a una historia abierta.
     *
     * @param historiaId  identificador de la historia.
     * @param anotacionRq datos de la anotación.
     * @return respuesta de éxito.
     */
    MiRespuestaRS guardarAnotacion(Long historiaId, AnotacionRq anotacionRq);
}
