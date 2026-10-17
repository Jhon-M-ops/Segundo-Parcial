package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones();

    MiRespuestaRS guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;

    MiRespuestaRS eliminarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException;
}