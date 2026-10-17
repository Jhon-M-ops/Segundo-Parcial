package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.ClienteService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<Cliente> listarClientes() {
        return clienteRepository.findAllByOrderByNombresAsc();
    }

    /** {@inheritDoc} */
    @Override
    public Cliente consultarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("El cliente no fue encontrado"));
    }

    @Override
    public MiRespuestaRS guardarCliente(ClienteRq clienteRq) throws BadRequestException {
        validarObjetoCliente(clienteRq);

        Optional<Cliente> optDuplicado = clienteRepository
                .findByNumeroDocumentoIgnoreCase(clienteRq.getNumeroDocumento());
        if (optDuplicado.isPresent()) {
            throw new BadRequestException(
                    "Ya existe un cliente con el número de documento: " + clienteRq.getNumeroDocumento());
        }

        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setTipoDocumento(clienteRq.getTipoDocumento());
        clienteNuevo.setNumeroDocumento(clienteRq.getNumeroDocumento());
        clienteNuevo.setNombres(clienteRq.getNombres());
        clienteNuevo.setApellidos(clienteRq.getApellidos());
        clienteNuevo.setFechaNacimiento(clienteRq.getFechaNacimiento());
        clienteNuevo.setGenero(clienteRq.getGenero());
        clienteNuevo.setTelefono(clienteRq.getTelefono());
        clienteNuevo.setDireccion(clienteRq.getDireccion());
        clienteNuevo.setActivo(clienteRq.getActivo() == null || clienteRq.getActivo());
        this.clienteRepository.save(clienteNuevo);

        return construirRespuesta("Cliente guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarCliente(ClienteRq clienteRq) throws BadRequestException {
        validarObjetoCliente(clienteRq);

        if (clienteRq.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente no puede ser nulo");
        }

        Optional<Cliente> optCliente = this.clienteRepository
                .findById(clienteRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new NotFoundException("Cliente no encontrado");
        }

        Optional<Cliente> optDuplicado = this.clienteRepository
                .findByNumeroDocumentoIgnoreCase(clienteRq.getNumeroDocumento());
        if (optDuplicado.isPresent() && !optDuplicado.get().getClienteId().equals(optCliente.get().getClienteId())) {
            throw new BadRequestException(
                    "Ya existe otro cliente con el número de documento: " + clienteRq.getNumeroDocumento());
        }

        Cliente clienteActualizar = optCliente.get();
        clienteActualizar.setTipoDocumento(clienteRq.getTipoDocumento());
        clienteActualizar.setNumeroDocumento(clienteRq.getNumeroDocumento());
        clienteActualizar.setNombres(clienteRq.getNombres());
        clienteActualizar.setApellidos(clienteRq.getApellidos());
        clienteActualizar.setFechaNacimiento(clienteRq.getFechaNacimiento());
        clienteActualizar.setGenero(clienteRq.getGenero());
        clienteActualizar.setTelefono(clienteRq.getTelefono());
        clienteActualizar.setDireccion(clienteRq.getDireccion());
        clienteActualizar.setActivo(clienteRq.getActivo() == null || clienteRq.getActivo());
        this.clienteRepository.save(clienteActualizar);

        return construirRespuesta("Cliente actualizado correctamente");
    }

    @Override
    public MiRespuestaRS eliminarCliente(ClienteRq clienteRq) throws BadRequestException {
        if (clienteRq == null || clienteRq.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente no puede ser nulo");
        }

        Optional<Cliente> optCliente = this.clienteRepository
                .findById(clienteRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new NotFoundException("Cliente no encontrado");
        }

        Cliente cliente = optCliente.get();
        boolean tieneMascotas = !this.mascotaRepository
                .findAllByClienteOrderByNombreMascotaAsc(cliente)
                .isEmpty();
        if (tieneMascotas) {
            throw new ConflictException("No se puede eliminar el cliente porque tiene mascotas registradas");
        }

        this.clienteRepository.delete(cliente);

        return construirRespuesta("Cliente eliminado correctamente");
    }

    private void validarObjetoCliente(ClienteRq clienteRq) throws BadRequestException {
        if (clienteRq == null) {
            throw new BadRequestException("Los datos enviados no pueden estar vacíos");
        }

        if (clienteRq.getTipoDocumento() == null || clienteRq.getTipoDocumento().isBlank()) {
            throw new BadRequestException("El tipo de documento del cliente no puede ser nulo o vacío");
        }

        if (clienteRq.getNumeroDocumento() == null || clienteRq.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El número de documento del cliente no puede ser nulo o vacío");
        }

        if (clienteRq.getNombres() == null || clienteRq.getNombres().isBlank()) {
            throw new BadRequestException("Los nombres del cliente no pueden ser nulos o vacíos");
        }

        if (clienteRq.getApellidos() == null || clienteRq.getApellidos().isBlank()) {
            throw new BadRequestException("Los apellidos del cliente no pueden ser nulos o vacíos");
        }

        if (clienteRq.getFechaNacimiento() == null) {
            throw new BadRequestException("La fecha de nacimiento del cliente no puede ser nula");
        }

        if (clienteRq.getFechaNacimiento().isAfter(java.time.LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento del cliente no puede ser futura");
        }
    }

    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage(mensaje);
        return rta;
    }
}