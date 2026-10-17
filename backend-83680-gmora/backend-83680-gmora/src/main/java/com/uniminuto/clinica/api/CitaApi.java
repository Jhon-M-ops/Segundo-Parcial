package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaDetalleRs;
import com.uniminuto.clinica.models.CitaEstadoRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaResumenRs;
import com.uniminuto.clinica.models.CitaRq;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST de las citas médicas.
 * <ul>
 *     <li>GET   /cita/listar                : listado paginado, filtrado y ordenado.</li>
 *     <li>GET   /cita/listar-by-mascota     : citas de una mascota (combo de fórmulas).</li>
 *     <li>GET   /cita/{id}                  : detalle.</li>
 *     <li>POST  /cita/guardar               : crear (201).</li>
 *     <li>PUT   /cita/{id}                  : editar según el estado.</li>
 *     <li>PATCH /cita/{id}/estado           : confirmar, atender, cancelar...</li>
 *     <li>PATCH /cita/{id}/reactivar        : reactivar una cita cancelada.</li>
 * </ul>
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<PaginaRs<CitaResumenRs>> listarCitas(@ModelAttribute CitaFiltroRq filtro);

    @GetMapping(value = "/listar-by-mascota", produces = {"application/json"})
    ResponseEntity<List<Cita>> listarCitasPorMascota(@RequestParam Integer mascotaId);

    @GetMapping(value = "/{citaId}", produces = {"application/json"})
    ResponseEntity<CitaDetalleRs> consultarCita(@PathVariable Long citaId);

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarCita(@RequestBody CitaRq citaRq);

    @PutMapping(value = "/{citaId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarCita(@PathVariable Long citaId, @RequestBody CitaRq citaRq);

    @PatchMapping(value = "/{citaId}/estado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> cambiarEstadoCita(@PathVariable Long citaId,
                                                    @RequestBody CitaEstadoRq citaEstadoRq);

    @PatchMapping(value = "/{citaId}/reactivar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> reactivarCita(@PathVariable Long citaId);
}
