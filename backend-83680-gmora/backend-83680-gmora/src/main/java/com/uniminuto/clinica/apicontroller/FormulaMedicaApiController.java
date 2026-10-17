package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaFiltroRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de fórmulas médicas. Solo traduce HTTP a llamadas al
 * servicio; la lógica de negocio vive en {@code FormulaMedicaServiceImpl}.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PaginaRs<FormulaResumenRs>> listarFormulas(FormulaFiltroRq filtro) {
        return ResponseEntity.ok(formulaService.listarFormulas(filtro));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FormulaDetalleRs> consultarFormula(Long formulaId) {
        return ResponseEntity.ok(formulaService.consultarFormula(formulaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarFormula(FormulaRq formulaRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaService.guardarFormula(formulaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormula(Long formulaId, FormulaRq formulaRq) {
        return ResponseEntity.ok(formulaService.actualizarFormula(formulaId, formulaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> inactivarFormula(Long formulaId) {
        return ResponseEntity.ok(formulaService.inactivarFormula(formulaId));
    }
}
