package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface MascotaService {

    List<Mascota> listarMascotas() throws BadRequestException;

    /**
     * Consulta una mascota por su identificador (404 si no existe).
     *
     * @param mascotaId identificador de la mascota.
     * @return la mascota.
     */
    Mascota consultarMascota(Integer mascotaId);

    List<Mascota> listarMascotasPorCliente(Long clienteId) throws BadRequestException;

    List<Mascota> listarMascotasPorRaza(Integer razaId) throws BadRequestException;

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS eliminarMascota(MascotaRq mascotaRq) throws BadRequestException;

}
