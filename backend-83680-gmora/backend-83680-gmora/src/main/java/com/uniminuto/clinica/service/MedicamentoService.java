package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface MedicamentoService {

    List<Medicamento> obtenerMedicamentos();

    MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;

    MiRespuestaRS eliminarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException;
}