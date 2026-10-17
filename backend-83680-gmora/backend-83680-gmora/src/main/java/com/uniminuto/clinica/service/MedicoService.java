package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface MedicoService {

    List<Medico> listarMedicos();

    MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException;

    MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException;

    MiRespuestaRS eliminarMedico(MedicoRq medicoRq) throws BadRequestException;
}