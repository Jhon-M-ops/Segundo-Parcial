package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaDetalleRs;
import com.uniminuto.clinica.models.CitaEstadoRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaResumenRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

import java.util.List;

/**
 * Casos de uso de la gestión de citas médicas.
 */
public interface CitaService {

    /**
     * Lista las citas con paginación, filtros y ordenamiento.
     *
     * @param filtro parámetros de consulta.
     * @return página con la información resumida de cada cita.
     */
    PaginaRs<CitaResumenRs> listarCitas(CitaFiltroRq filtro);

    /**
     * Consulta el detalle de una cita.
     *
     * @param citaId identificador de la cita.
     * @return detalle de la cita.
     */
    CitaDetalleRs consultarCita(Long citaId);

    /**
     * Registra una cita validando todas las reglas de negocio.
     *
     * @param citaRq datos de la cita.
     * @return respuesta de éxito.
     */
    MiRespuestaRS guardarCita(CitaRq citaRq);

    /**
     * Modifica una cita según su estado actual.
     *
     * @param citaId identificador de la cita.
     * @param citaRq nuevos datos.
     * @return respuesta de éxito.
     */
    MiRespuestaRS actualizarCita(Long citaId, CitaRq citaRq);

    /**
     * Cambia el estado de una cita respetando las transiciones permitidas.
     *
     * @param citaId       identificador de la cita.
     * @param citaEstadoRq estado destino.
     * @return respuesta de éxito.
     */
    MiRespuestaRS cambiarEstado(Long citaId, CitaEstadoRq citaEstadoRq);

    /**
     * Reactiva una cita cancelada (operación explícita). Vuelve a Pendiente
     * y se revalidan fecha y disponibilidad.
     *
     * @param citaId identificador de la cita.
     * @return respuesta de éxito.
     */
    MiRespuestaRS reactivarCita(Long citaId);

    /**
     * Lista las citas de una mascota (usado por el combo de fórmulas).
     *
     * @param mascotaId identificador de la mascota.
     * @return citas de la mascota, de la más reciente a la más antigua.
     */
    List<Cita> listarCitasPorMascota(Integer mascotaId);
}
