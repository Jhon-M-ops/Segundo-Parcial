package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaClinicaApi;
import com.uniminuto.clinica.models.AnotacionFiltroRq;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaDetalleRs;
import com.uniminuto.clinica.models.HistoriaFiltroRq;
import com.uniminuto.clinica.models.HistoriaResumenRs;
import com.uniminuto.clinica.models.HistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de historias clínicas y anotaciones. Traduce HTTP a llamadas
 * al servicio; las reglas de negocio viven en {@code HistoriaClinicaServiceImpl}.
 */
@RestController
public class HistoriaClinicaApiController implements HistoriaClinicaApi {

    @Autowired
    private HistoriaClinicaService historiaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PaginaRs<HistoriaResumenRs>> listarHistorias(HistoriaFiltroRq filtro) {
        return ResponseEntity.ok(historiaService.listarHistorias(filtro));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<HistoriaDetalleRs> consultarHistoria(Long historiaId) {
        return ResponseEntity.ok(historiaService.consultarHistoria(historiaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoria(HistoriaRq historiaRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.guardarHistoria(historiaRq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> cerrarHistoria(Long historiaId) {
        return ResponseEntity.ok(historiaService.cerrarHistoria(historiaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PaginaRs<AnotacionRs>> listarAnotaciones(Long historiaId, AnotacionFiltroRq filtro) {
        return ResponseEntity.ok(historiaService.listarAnotaciones(historiaId, filtro));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AnotacionRs> consultarAnotacion(Long historiaId, Long anotacionId) {
        return ResponseEntity.ok(historiaService.consultarAnotacion(historiaId, anotacionId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(Long historiaId, AnotacionRq anotacionRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaService.guardarAnotacion(historiaId, anotacionRq));
    }
}
