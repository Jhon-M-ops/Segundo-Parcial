package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<Especializacion> listarEspecializaciones() {
        return this.especializacionRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public MiRespuestaRS guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        validarObjetoEspecializacion(especializacionRq);

        Optional<Especializacion> optNombre = this.especializacionRepository
                .findByNombreIgnoreCase(especializacionRq.getNombre());
        if (optNombre.isPresent()) {
            throw new BadRequestException(
                    "Ya existe una especialización con el nombre: " + especializacionRq.getNombre());
        }

        Optional<Especializacion> optCodigo = this.especializacionRepository
                .findByCodigoEspecializacionIgnoreCase(especializacionRq.getCodigoEspecializacion());
        if (optCodigo.isPresent()) {
            throw new BadRequestException(
                    "Ya existe una especialización con el código: " + especializacionRq.getCodigoEspecializacion());
        }

        Especializacion especializacionNueva = new Especializacion();
        especializacionNueva.setNombre(especializacionRq.getNombre());
        especializacionNueva.setDescripcion(especializacionRq.getDescripcion());
        especializacionNueva.setCodigoEspecializacion(especializacionRq.getCodigoEspecializacion());
        this.especializacionRepository.save(especializacionNueva);

        return construirRespuesta("Especialización guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        validarObjetoEspecializacion(especializacionRq);

        if (especializacionRq.getId() == null) {
            throw new BadRequestException("El ID de la especialización no puede ser nulo");
        }

        Optional<Especializacion> optEspecializacion = this.especializacionRepository
                .findById(especializacionRq.getId());
        if (optEspecializacion.isEmpty()) {
            throw new NotFoundException("Especialización no encontrada");
        }

        Optional<Especializacion> optNombre = this.especializacionRepository
                .findByNombreIgnoreCase(especializacionRq.getNombre());
        if (optNombre.isPresent() && !optNombre.get().getId().equals(optEspecializacion.get().getId())) {
            throw new BadRequestException(
                    "Ya existe otra especialización con el nombre: " + especializacionRq.getNombre());
        }

        Optional<Especializacion> optCodigo = this.especializacionRepository
                .findByCodigoEspecializacionIgnoreCase(especializacionRq.getCodigoEspecializacion());
        if (optCodigo.isPresent() && !optCodigo.get().getId().equals(optEspecializacion.get().getId())) {
            throw new BadRequestException(
                    "Ya existe otra especialización con el código: " + especializacionRq.getCodigoEspecializacion());
        }

        Especializacion especializacionActualizar = optEspecializacion.get();
        especializacionActualizar.setNombre(especializacionRq.getNombre());
        especializacionActualizar.setDescripcion(especializacionRq.getDescripcion());
        especializacionActualizar.setCodigoEspecializacion(especializacionRq.getCodigoEspecializacion());
        this.especializacionRepository.save(especializacionActualizar);

        return construirRespuesta("Especialización actualizada correctamente");
    }

    @Override
    public MiRespuestaRS eliminarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        if (especializacionRq == null || especializacionRq.getId() == null) {
            throw new BadRequestException("El ID de la especialización no puede ser nulo");
        }

        Optional<Especializacion> optEspecializacion = this.especializacionRepository
                .findById(especializacionRq.getId());
        if (optEspecializacion.isEmpty()) {
            throw new NotFoundException("Especialización no encontrada");
        }

        Especializacion especializacion = optEspecializacion.get();
        boolean tieneMedicos = !this.medicoRepository
                .findAllByEspecializacionOrderByNombresAsc(especializacion)
                .isEmpty();
        if (tieneMedicos) {
            throw new ConflictException("No se puede eliminar la especialización porque tiene médicos asociados");
        }

        this.especializacionRepository.delete(especializacion);

        return construirRespuesta("Especialización eliminada correctamente");
    }

    private void validarObjetoEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        if (especializacionRq == null) {
            throw new BadRequestException("Los datos enviados no pueden estar vacíos");
        }

        if (especializacionRq.getNombre() == null || especializacionRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la especialización no puede ser nulo o vacío");
        }

        if (especializacionRq.getCodigoEspecializacion() == null
                || especializacionRq.getCodigoEspecializacion().isBlank()) {
            throw new BadRequestException("El código de la especialización no puede ser nulo o vacío");
        }
    }

    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage(mensaje);
        return rta;
    }
}