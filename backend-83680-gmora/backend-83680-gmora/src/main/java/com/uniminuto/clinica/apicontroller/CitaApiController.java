package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaDetalleRs;
import com.uniminuto.clinica.models.CitaEstadoRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaResumenRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de citas. Traduce HTTP a llamadas al servicio; las reglas
 * de negocio viven en {@code CitaServiceImpl}.
 */
@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PaginaRs<CitaResumenRs>> listarCitas(CitaFiltroRq filtro) {
        return ResponseEntity.ok(citaService.listarCitas(filtro));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Cita>> listarCitasPorMascota(Integer mascotaId) {
        return ResponseEntity.ok(citaService.listarCitasPorMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<CitaDetalleRs> consultarCita(Long citaId) {
        return ResponseEntity.ok(citaService.consultarCita(citaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.guardarCita(citaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(Long citaId, CitaRq citaRq) {
        return ResponseEntity.ok(citaService.actualizarCita(citaId, citaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> cambiarEstadoCita(Long citaId, CitaEstadoRq citaEstadoRq) {
        return ResponseEntity.ok(citaService.cambiarEstado(citaId, citaEstadoRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> reactivarCita(Long citaId) {
        return ResponseEntity.ok(citaService.reactivarCita(citaId));
    }
}
