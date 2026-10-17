package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private FormulaMedicaRepository formulaRepository;

    @Override
    public List<Medicamento> obtenerMedicamentos() {
        return this.medicamentoRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        validarObjetoMedicamento(medicamentoRq);

        Optional<Medicamento> optDuplicado = this.medicamentoRepository
                .findByNombreIgnoreCase(medicamentoRq.getNombre());
        if (optDuplicado.isPresent()) {
            throw new BadRequestException("Ya existe un medicamento con el nombre: " + medicamentoRq.getNombre());
        }

        Medicamento medicamentoNuevo = new Medicamento();
        this.aplicarDatosMedicamento(medicamentoNuevo, medicamentoRq);
        medicamentoNuevo.setFechaCreacionRegistro(LocalDateTime.now());
        this.medicamentoRepository.save(medicamentoNuevo);

        return construirRespuesta("Medicamento guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        validarObjetoMedicamento(medicamentoRq);

        if (medicamentoRq.getId() == null) {
            throw new BadRequestException("El ID del medicamento no puede ser nulo");
        }

        Optional<Medicamento> optMedicamento = this.medicamentoRepository
                .findById(medicamentoRq.getId());
        if (optMedicamento.isEmpty()) {
            throw new NotFoundException("Medicamento no encontrado");
        }

        Optional<Medicamento> optDuplicado = this.medicamentoRepository
                .findByNombreIgnoreCase(medicamentoRq.getNombre());
        if (optDuplicado.isPresent() && !optDuplicado.get().getId().equals(optMedicamento.get().getId())) {
            throw new BadRequestException("Ya existe otro medicamento con el nombre: " + medicamentoRq.getNombre());
        }

        Medicamento medicamentoActualizar = optMedicamento.get();
        this.aplicarDatosMedicamento(medicamentoActualizar, medicamentoRq);
        medicamentoActualizar.setFechaModificacionRegistro(LocalDateTime.now());
        this.medicamentoRepository.save(medicamentoActualizar);

        return construirRespuesta("Medicamento actualizado correctamente");
    }

    @Override
    public MiRespuestaRS eliminarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        if (medicamentoRq == null || medicamentoRq.getId() == null) {
            throw new BadRequestException("El ID del medicamento no puede ser nulo");
        }

        Optional<Medicamento> optMedicamento = this.medicamentoRepository
                .findById(medicamentoRq.getId());
        if (optMedicamento.isEmpty()) {
            throw new NotFoundException("Medicamento no encontrado");
        }

        // Integridad: no se elimina un medicamento que ya fue formulado.
        Medicamento medicamento = optMedicamento.get();
        if (this.formulaRepository.existsByMedicamento(medicamento)) {
            throw new ConflictException(
                    "No se puede eliminar el medicamento porque está incluido en fórmulas médicas");
        }

        this.medicamentoRepository.delete(medicamento);

        return construirRespuesta("Medicamento eliminado correctamente");
    }

    private void aplicarDatosMedicamento(Medicamento medicamento, MedicamentoRq medicamentoRq) {
        medicamento.setNombre(medicamentoRq.getNombre());
        medicamento.setDescripcion(medicamentoRq.getDescripcion());
        medicamento.setPresentacion(medicamentoRq.getPresentacion());
        medicamento.setFechaCompra(medicamentoRq.getFechaCompra());
        medicamento.setFechaVence(medicamentoRq.getFechaVence());
    }

    private void validarObjetoMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        if (medicamentoRq == null) {
            throw new BadRequestException("Los datos enviados no pueden estar vacíos");
        }

        if (medicamentoRq.getNombre() == null || medicamentoRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento no puede ser nulo o vacío");
        }

        if (medicamentoRq.getFechaVence() != null && medicamentoRq.getFechaCompra() != null
                && medicamentoRq.getFechaVence().isBefore(medicamentoRq.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de compra");
        }
    }

    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage(mensaje);
        return rta;
    }
}