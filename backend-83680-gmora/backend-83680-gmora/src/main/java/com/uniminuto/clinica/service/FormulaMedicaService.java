package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaFiltroRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

/**
 * Casos de uso de la gestión de fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista las fórmulas con paginación, filtros y ordenamiento.
     *
     * @param filtro parámetros de consulta.
     * @return página con la información resumida de cada fórmula.
     */
    PaginaRs<FormulaResumenRs> listarFormulas(FormulaFiltroRq filtro);

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param formulaId identificador de la fórmula.
     * @return detalle de la fórmula.
     */
    FormulaDetalleRs consultarFormula(Long formulaId);

    /**
     * Registra una nueva fórmula validando todas las reglas de negocio.
     *
     * @param formulaRq datos de la fórmula.
     * @return respuesta de éxito.
     */
    MiRespuestaRS guardarFormula(FormulaRq formulaRq);

    /**
     * Modifica una fórmula activa. La mascota, la cita y el veterinario no
     * pueden cambiarse.
     *
     * @param formulaId identificador de la fórmula.
     * @param formulaRq nuevos datos.
     * @return respuesta de éxito.
     */
    MiRespuestaRS actualizarFormula(Long formulaId, FormulaRq formulaRq);

    /**
     * Inactiva una fórmula (reemplaza la eliminación física).
     *
     * @param formulaId identificador de la fórmula.
     * @return respuesta de éxito.
     */
    MiRespuestaRS inactivarFormula(Long formulaId);
}
