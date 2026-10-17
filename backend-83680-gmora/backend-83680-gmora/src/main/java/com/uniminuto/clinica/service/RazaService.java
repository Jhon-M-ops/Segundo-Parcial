package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface RazaService {

    List<Raza> listarRazas();

    List<Raza> listarRazasPorEspecie(String especie) throws BadRequestException;

    MiRespuestaRS guardarRazaNueva(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException;

    MiRespuestaRS eliminarRaza(RazaRq razaRq) throws BadRequestException;
}