package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<Raza> listarRazas() {
        return this.razaRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<Raza> listarRazasPorEspecie(String especie) throws BadRequestException {
        if (especie == null || especie.isBlank()) {
            throw new BadRequestException("La especie de la raza no puede ser nula o vacía");
        }

        return this.razaRepository.findAllByEspecieOrderByNombreAsc(especie);
    }

    @Override
    public MiRespuestaRS guardarRazaNueva(RazaRq razaRq) throws BadRequestException {
        validarObjetoEntrada(razaRq);

        Optional<Raza> optRaza = this.razaRepository
                .findByNombreIgnoreCase(razaRq.getNombre());
        if (optRaza.isPresent()) {
            throw new BadRequestException("Ya existe una raza con el nombre: " + razaRq.getNombre());
        }

        Raza razaNueva = new Raza();
        razaNueva.setNombre(razaRq.getNombre());
        razaNueva.setEspecie(razaRq.getEspecie());
        razaNueva.setFechaCreacion(LocalDateTime.now());
        this.razaRepository.save(razaNueva);

        return construirRespuesta("Raza creada exitosamente");
    }

    @Override
    public MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException {
        validarObjetoEntrada(razaRq);

        if (razaRq.getRazaId() == null) {
            throw new BadRequestException("El ID de la raza no puede ser nulo");
        }

        Optional<Raza> optRaza = this.razaRepository.findById(razaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new NotFoundException("Raza no encontrada");
        }

        Optional<Raza> optDuplicado = this.razaRepository.findByNombreIgnoreCase(razaRq.getNombre());
        if (optDuplicado.isPresent() && !optDuplicado.get().getRazaId().equals(optRaza.get().getRazaId())) {
            throw new BadRequestException("Ya existe otra raza con el nombre: " + razaRq.getNombre());
        }

        Raza razaActualizar = optRaza.get();
        razaActualizar.setNombre(razaRq.getNombre());
        razaActualizar.setEspecie(razaRq.getEspecie());
        razaActualizar.setFechaModificacion(LocalDateTime.now());
        this.razaRepository.save(razaActualizar);

        return construirRespuesta("Raza actualizada exitosamente");
    }

    @Override
    public MiRespuestaRS eliminarRaza(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null || razaRq.getRazaId() == null) {
            throw new BadRequestException("El ID de la raza no puede ser nulo");
        }

        Optional<Raza> optRaza = this.razaRepository.findById(razaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new NotFoundException("Raza no encontrada");
        }

        Raza raza = optRaza.get();
        boolean tieneMascotas = !this.mascotaRepository
                .findAllByRazaOrderByNombreMascotaAsc(raza)
                .isEmpty();
        if (tieneMascotas) {
            throw new ConflictException("No se puede eliminar la raza porque tiene mascotas registradas");
        }

        this.razaRepository.delete(raza);

        return construirRespuesta("Raza eliminada exitosamente");
    }

    private void validarObjetoEntrada(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null) {
            throw new BadRequestException("Los datos enviados no pueden estar vacíos");
        }

        if (razaRq.getNombre() == null || razaRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la raza no puede ser nulo o vacío");
        }

        if (razaRq.getEspecie() == null || razaRq.getEspecie().isBlank()) {
            throw new BadRequestException("La especie de la raza no puede ser nulo o vacío");
        }
    }

    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage(mensaje);
        return rta;
    }
}