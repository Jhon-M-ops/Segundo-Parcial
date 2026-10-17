package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.EspecializacionService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    @Autowired
    private EspecializacionService especializacionService;

    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() {
        return ResponseEntity.ok(this.especializacionService.listarEspecializaciones());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarEspecializacion(EspecializacionRq especializacionRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.guardarEspecializacion(especializacionRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(EspecializacionRq especializacionRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.actualizarEspecializacion(especializacionRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarEspecializacion(EspecializacionRq especializacionRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.eliminarEspecializacion(especializacionRq));
    }
}