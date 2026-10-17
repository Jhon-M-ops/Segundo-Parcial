package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaClinicaRepository;
import com.uniminuto.clinica.repository.AnotacionRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HistoriaClinicaRepository historiaRepository;

    @Autowired
    private AnotacionRepository anotacionRepository;

    @Autowired
    private FormulaMedicaRepository formulaRepository;

    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAllByOrderByNombresAsc();
    }

    @Override
    public MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException {
        validarObjetoMedico(medicoRq);

        Optional<Medico> optDuplicado = this.medicoRepository
                .findByNumeroDocumentoIgnoreCase(medicoRq.getNumeroDocumento());
        if (optDuplicado.isPresent()) {
            throw new BadRequestException(
                    "Ya existe un médico con el número de documento: " + medicoRq.getNumeroDocumento());
        }

        Optional<Especializacion> optEspecializacion = this.especializacionRepository
                .findById(medicoRq.getEspecializacionId());
        if (optEspecializacion.isEmpty()) {
            throw new NotFoundException("Especialización no encontrada");
        }

        Medico medicoNuevo = new Medico();
        this.aplicarDatosMedico(medicoNuevo, medicoRq, optEspecializacion.get());
        this.medicoRepository.save(medicoNuevo);

        return construirRespuesta("Médico guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        validarObjetoMedico(medicoRq);

        if (medicoRq.getId() == null) {
            throw new BadRequestException("El ID del médico no puede ser nulo");
        }

        Optional<Medico> optMedico = this.medicoRepository.findById(medicoRq.getId());
        if (optMedico.isEmpty()) {
            throw new NotFoundException("Médico no encontrado");
        }

        Optional<Medico> optDuplicado = this.medicoRepository
                .findByNumeroDocumentoIgnoreCase(medicoRq.getNumeroDocumento());
        if (optDuplicado.isPresent() && !optDuplicado.get().getId().equals(optMedico.get().getId())) {
            throw new BadRequestException(
                    "Ya existe otro médico con el número de documento: " + medicoRq.getNumeroDocumento());
        }

        Optional<Especializacion> optEspecializacion = this.especializacionRepository
                .findById(medicoRq.getEspecializacionId());
        if (optEspecializacion.isEmpty()) {
            throw new NotFoundException("Especialización no encontrada");
        }

        this.aplicarDatosMedico(optMedico.get(), medicoRq, optEspecializacion.get());
        this.medicoRepository.save(optMedico.get());

        return construirRespuesta("Médico actualizado correctamente");
    }

    @Override
    public MiRespuestaRS eliminarMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq == null || medicoRq.getId() == null) {
            throw new BadRequestException("El ID del médico no puede ser nulo");
        }

        Optional<Medico> optMedico = this.medicoRepository.findById(medicoRq.getId());
        if (optMedico.isEmpty()) {
            throw new NotFoundException("Médico no encontrado");
        }

        // Integridad: no se elimina un veterinario con actividad clínica registrada.
        Medico medico = optMedico.get();
        if (this.citaRepository.existsByMedico(medico)
                || this.historiaRepository.existsByMedico(medico)
                || this.anotacionRepository.existsByMedico(medico)
                || this.formulaRepository.existsByMedico(medico)) {
            throw new ConflictException(
                    "No se puede eliminar el veterinario porque tiene citas, historias, anotaciones o fórmulas registradas");
        }

        this.medicoRepository.delete(medico);

        return construirRespuesta("Médico eliminado correctamente");
    }

    private void aplicarDatosMedico(Medico medico, MedicoRq medicoRq, Especializacion especializacion) {
        medico.setTipoDocumento(medicoRq.getTipoDocumento());
        medico.setNumeroDocumento(medicoRq.getNumeroDocumento());
        medico.setNombres(medicoRq.getNombres());
        medico.setApellidos(medicoRq.getApellidos());
        medico.setTelefono(medicoRq.getTelefono());
        medico.setRegistroProfesional(medicoRq.getRegistroProfesional());
        medico.setEspecializacion(especializacion);
    }

    private void validarObjetoMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq == null) {
            throw new BadRequestException("Los datos enviados no pueden estar vacíos");
        }

        if (medicoRq.getTipoDocumento() == null || medicoRq.getTipoDocumento().isBlank()) {
            throw new BadRequestException("El tipo de documento del médico no puede ser nulo o vacío");
        }

        if (medicoRq.getNumeroDocumento() == null || medicoRq.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El número de documento del médico no puede ser nulo o vacío");
        }

        if (medicoRq.getNombres() == null || medicoRq.getNombres().isBlank()) {
            throw new BadRequestException("Los nombres del médico no pueden ser nulos o vacíos");
        }

        if (medicoRq.getApellidos() == null || medicoRq.getApellidos().isBlank()) {
            throw new BadRequestException("Los apellidos del médico no pueden ser nulos o vacíos");
        }

        if (medicoRq.getRegistroProfesional() == null || medicoRq.getRegistroProfesional().isBlank()) {
            throw new BadRequestException("El registro profesional del médico no puede ser nulo o vacío");
        }

        if (medicoRq.getEspecializacionId() == null) {
            throw new BadRequestException("El ID de la especialización del médico no puede ser nulo");
        }
    }

    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage(mensaje);
        return rta;
    }
}