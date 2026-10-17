package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.AnotacionFiltroRq;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaDetalleRs;
import com.uniminuto.clinica.models.HistoriaFiltroRq;
import com.uniminuto.clinica.models.HistoriaResumenRs;
import com.uniminuto.clinica.models.HistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrato REST de historias clínicas y anotaciones.
 * <ul>
 *     <li>GET   /historia-clinica/listar                          : listado paginado, filtrado y ordenado.</li>
 *     <li>GET   /historia-clinica/{id}                            : detalle.</li>
 *     <li>POST  /historia-clinica/guardar                         : crear (201).</li>
 *     <li>PATCH /historia-clinica/{id}/cerrar                     : cerrar la historia.</li>
 *     <li>GET   /historia-clinica/{id}/anotaciones                : histórico paginado de anotaciones.</li>
 *     <li>GET   /historia-clinica/{id}/anotaciones/{anotacionId}  : detalle de una anotación.</li>
 *     <li>POST  /historia-clinica/{id}/anotaciones                : agregar anotación (201).</li>
 * </ul>
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia-clinica")
public interface HistoriaClinicaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<HistoriaResumenRs>> listarHistorias(@ModelAttribute HistoriaFiltroRq filtro);

    @GetMapping(value = "/{historiaId}", produces = {"application/json"})
    ResponseEntity<HistoriaDetalleRs> consultarHistoria(@PathVariable Long historiaId);

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarHistoria(@RequestBody HistoriaRq historiaRq);

    @PatchMapping(value = "/{historiaId}/cerrar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> cerrarHistoria(@PathVariable Long historiaId);

    @GetMapping(value = "/{historiaId}/anotaciones", produces = {"application/json"})
    ResponseEntity<PaginaRs<AnotacionRs>> listarAnotaciones(@PathVariable Long historiaId,
                                                            @ModelAttribute AnotacionFiltroRq filtro);

    @GetMapping(value = "/{historiaId}/anotaciones/{anotacionId}", produces = {"application/json"})
    ResponseEntity<AnotacionRs> consultarAnotacion(@PathVariable Long historiaId,
                                                   @PathVariable Long anotacionId);

    @PostMapping(value = "/{historiaId}/anotaciones",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarAnotacion(@PathVariable Long historiaId,
                                                   @RequestBody AnotacionRq anotacionRq);
}
