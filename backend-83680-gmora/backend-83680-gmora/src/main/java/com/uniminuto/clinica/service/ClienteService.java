package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface ClienteService {

    List<Cliente> listarClientes();

    /**
     * Consulta un cliente por su identificador (404 si no existe).
     *
     * @param clienteId identificador del cliente.
     * @return el cliente.
     */
    Cliente consultarCliente(Long clienteId);

    MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException;

    MiRespuestaRS eliminarCliente(ClienteRq clienteRq) throws BadRequestException;
}