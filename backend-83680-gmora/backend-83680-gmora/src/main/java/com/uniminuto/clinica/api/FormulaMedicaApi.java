package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaFiltroRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato REST de las fórmulas médicas.
 * <ul>
 *     <li>GET   /formula/listar              : listado paginado, filtrado y ordenado.</li>
 *     <li>GET   /formula/{id}                : detalle.</li>
 *     <li>POST  /formula/guardar             : crear (201).</li>
 *     <li>PUT   /formula/{id}                : editar.</li>
 *     <li>PATCH /formula/{id}/inactivar      : inactivar (no existe DELETE).</li>
 * </ul>
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula")
public interface FormulaMedicaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<FormulaResumenRs>> listarFormulas(@ModelAttribute FormulaFiltroRq filtro);

    @GetMapping(value = "/{formulaId}", produces = {"application/json"})
    ResponseEntity<FormulaDetalleRs> consultarFormula(@PathVariable Long formulaId);

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormula(@RequestBody FormulaRq formulaRq);

    @PutMapping(value = "/{formulaId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula(@PathVariable Long formulaId,
                                                    @RequestBody FormulaRq formulaRq);

    @PatchMapping(value = "/{formulaId}/inactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> inactivarFormula(@PathVariable Long formulaId);
}
