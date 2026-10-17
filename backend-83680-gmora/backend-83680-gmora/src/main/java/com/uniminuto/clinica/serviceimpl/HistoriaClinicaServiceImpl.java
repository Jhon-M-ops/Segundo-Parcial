package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Anotacion;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.HistoriaClinica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.enums.EstadoCita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.AnotacionFiltroRq;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaDetalleRs;
import com.uniminuto.clinica.models.HistoriaFiltroRq;
import com.uniminuto.clinica.models.HistoriaResumenRs;
import com.uniminuto.clinica.models.HistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.AnotacionRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaClinicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * Implementación de la gestión de historias clínicas y anotaciones. Aquí viven las
 * reglas de negocio; el frontend solo orienta al usuario.
 */
@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    private static final int TAMANO_MAXIMO_PAGINA = 100;

    /** Estados válidos de una historia (para validar el filtro). */
    private static final Set<String> ESTADOS = Set.of(HistoriaClinica.ABIERTA, HistoriaClinica.CERRADA);

    /** Lista blanca de columnas ordenables del listado de historias. */
    private static final Map<String, String> CAMPOS_ORDEN_HISTORIA = Map.of(
            "fechaHora", "fechaHora",
            "fecha", "fechaHora",
            "mascota", "mascota.nombreMascota",
            "cliente", "mascota.cliente.nombres",
            "veterinario", "medico.nombres",
            "motivo", "motivo",
            "estado", "estado");

    @Autowired
    private HistoriaClinicaRepository historiaRepository;

    @Autowired
    private AnotacionRepository anotacionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private FormulaMedicaRepository formulaRepository;

    // =====================================================================
    // Historias clínicas
    // =====================================================================

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<HistoriaResumenRs> listarHistorias(HistoriaFiltroRq filtro) {
        String campo = CAMPOS_ORDEN_HISTORIA.getOrDefault(filtro.getSortBy(), "fechaHora");
        Sort.Direction direccion = "asc".equalsIgnoreCase(filtro.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort orden = Sort.by(direccion, campo).and(Sort.by(Sort.Direction.DESC, "historiaId"));
        Pageable pageable = PageRequest.of(Math.max(filtro.getPage(), 0),
                Math.min(Math.max(filtro.getSize(), 1), TAMANO_MAXIMO_PAGINA), orden);

        String texto = patronTexto(filtro.getTexto());

        String estado = null;
        if (filtro.getEstado() != null && !filtro.getEstado().isBlank()) {
            estado = filtro.getEstado().trim().toUpperCase();
            if (!ESTADOS.contains(estado)) {
                throw new BadRequestException("El estado indicado en el filtro no es válido");
            }
        }
        LocalDateTime desde = filtro.getFechaDesde() == null ? null : filtro.getFechaDesde().atStartOfDay();
        LocalDateTime hasta = filtro.getFechaHasta() == null ? null : filtro.getFechaHasta().plusDays(1).atStartOfDay();

        Page<HistoriaClinica> resultado = this.historiaRepository.buscar(
                texto, filtro.getMedicoId(), estado, desde, hasta, pageable);

        return new PaginaRs<>(
                resultado.getContent().stream().map(this::aResumen).toList(),
                resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public HistoriaDetalleRs consultarHistoria(Long historiaId) {
        return this.aDetalle(this.buscarHistoriaOFallar(historiaId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarHistoria(HistoriaRq rq) {
        this.validarCamposHistoria(rq);

        // El cliente debe existir.
        Cliente cliente = this.clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no existe"));

        // La mascota debe existir y pertenecer al cliente.
        Mascota mascota = this.mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe"));
        if (mascota.getCliente() == null
                || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente");
        }

        // La cita debe existir y ser de esa mascota.
        Cita cita = this.citaRepository.findById(rq.getCitaId())
                .orElseThrow(() -> new BadRequestException("La cita seleccionada no existe"));
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada pertenece a otra mascota");
        }

        // Solo una cita atendida genera historia clínica.
        if (!EstadoCita.ATENDIDA.name().equalsIgnoreCase(cita.getEstado())) {
            throw new ConflictException("Solo se puede crear la historia clínica de una cita atendida");
        }

        // Una cita genera una sola historia (evita duplicidad).
        if (this.historiaRepository.existsByCita(cita)) {
            throw new ConflictException("La cita seleccionada ya tiene una historia clínica registrada");
        }

        Medico medico = this.medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe"));

        LocalDateTime fechaHora = rq.getFechaHora().truncatedTo(ChronoUnit.MINUTES);
        if (fechaHora.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha y hora de la consulta no pueden ser futuras");
        }
        if (fechaHora.toLocalDate().isBefore(cita.getFechaHora().toLocalDate())) {
            throw new BadRequestException("La fecha de la consulta no puede ser anterior a la fecha de la cita");
        }

        HistoriaClinica historia = new HistoriaClinica();
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setMedico(medico);
        historia.setFechaHora(fechaHora);
        historia.setMotivo(rq.getMotivo().trim());
        historia.setDiagnostico(rq.getDiagnostico().trim());
        historia.setObservaciones(rq.getObservaciones() == null || rq.getObservaciones().isBlank()
                ? null : rq.getObservaciones().trim());
        historia.setEstado(HistoriaClinica.ABIERTA);
        historia.setFechaCreacion(LocalDateTime.now());
        this.historiaRepository.save(historia);

        // Relación Historia 1:N Fórmula: las fórmulas ya emitidas en esta consulta quedan enlazadas.
        this.formulaRepository.vincularConHistoria(cita, historia);

        return this.construirRespuesta(201, "Historia clínica registrada correctamente");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS cerrarHistoria(Long historiaId) {
        HistoriaClinica historia = this.buscarHistoriaOFallar(historiaId);
        if (HistoriaClinica.CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("La historia clínica ya se encuentra cerrada");
        }
        historia.setEstado(HistoriaClinica.CERRADA);
        this.historiaRepository.save(historia);
        return this.construirRespuesta(200, "Historia clínica cerrada correctamente");
    }

    // =====================================================================
    // Anotaciones
    // =====================================================================

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<AnotacionRs> listarAnotaciones(Long historiaId, AnotacionFiltroRq filtro) {
        this.buscarHistoriaOFallar(historiaId);

        Sort.Direction direccion = "asc".equalsIgnoreCase(filtro.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort orden = Sort.by(direccion, "fechaHora").and(Sort.by(direccion, "anotacionId"));
        Pageable pageable = PageRequest.of(Math.max(filtro.getPage(), 0),
                Math.min(Math.max(filtro.getSize(), 1), TAMANO_MAXIMO_PAGINA), orden);

        LocalDateTime desde = filtro.getFechaDesde() == null ? null : filtro.getFechaDesde().atStartOfDay();
        LocalDateTime hasta = filtro.getFechaHasta() == null ? null : filtro.getFechaHasta().plusDays(1).atStartOfDay();

        Page<Anotacion> resultado = this.anotacionRepository.buscarPorHistoria(
                historiaId, patronTexto(filtro.getTexto()), desde, hasta, pageable);

        return new PaginaRs<>(
                resultado.getContent().stream().map(this::aAnotacionRs).toList(),
                resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public AnotacionRs consultarAnotacion(Long historiaId, Long anotacionId) {
        this.buscarHistoriaOFallar(historiaId);
        Anotacion anotacion = this.anotacionRepository.buscarDeHistoria(anotacionId, historiaId)
                .orElseThrow(() -> new NotFoundException("La anotación no fue encontrada"));
        return this.aAnotacionRs(anotacion);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarAnotacion(Long historiaId, AnotacionRq rq) {
        HistoriaClinica historia = this.buscarHistoriaOFallar(historiaId);

        if (HistoriaClinica.CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("La historia clínica está cerrada y no admite nuevas anotaciones");
        }
        this.validarCamposAnotacion(rq);

        Medico medico = this.medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe"));

        Anotacion anotacion = new Anotacion();
        anotacion.setHistoriaClinica(historia);
        anotacion.setMedico(medico);
        anotacion.setFechaHora(LocalDateTime.now());   // la asigna el servidor: no se puede falsear
        anotacion.setDescripcion(rq.getDescripcion().trim());
        anotacion.setObservacion(rq.getObservacion() == null || rq.getObservacion().isBlank()
                ? null : rq.getObservacion().trim());
        anotacion.setDiagnosticoEvolucion(rq.getDiagnosticoEvolucion().trim());
        this.anotacionRepository.save(anotacion);

        return this.construirRespuesta(201, "Anotación registrada correctamente");
    }

    // =====================================================================
    // Validaciones y utilidades privadas
    // =====================================================================

    /**
     * Valida los campos obligatorios de una historia.
     *
     * @param rq datos recibidos.
     */
    private void validarCamposHistoria(HistoriaRq rq) {
        if (rq == null) {
            throw new BadRequestException("Los datos de la historia clínica son obligatorios");
        }
        if (rq.getClienteId() == null) {
            throw new BadRequestException("El cliente es obligatorio");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria");
        }
        if (rq.getCitaId() == null) {
            throw new BadRequestException("La cita es obligatoria");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la consulta son obligatorias");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank() || rq.getMotivo().length() > 255) {
            throw new BadRequestException("El motivo de la consulta es obligatorio (máximo 255 caracteres)");
        }
        if (rq.getDiagnostico() == null || rq.getDiagnostico().isBlank() || rq.getDiagnostico().length() > 2000) {
            throw new BadRequestException("El diagnóstico es obligatorio (máximo 2000 caracteres)");
        }
        if (rq.getObservaciones() != null && rq.getObservaciones().length() > 2000) {
            throw new BadRequestException("Las observaciones no pueden superar los 2000 caracteres");
        }
    }

    /**
     * Valida los campos obligatorios de una anotación.
     *
     * @param rq datos recibidos.
     */
    private void validarCamposAnotacion(AnotacionRq rq) {
        if (rq == null) {
            throw new BadRequestException("Los datos de la anotación son obligatorios");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio");
        }
        if (rq.getDescripcion() == null || rq.getDescripcion().isBlank() || rq.getDescripcion().length() > 500) {
            throw new BadRequestException("La descripción es obligatoria (máximo 500 caracteres)");
        }
        if (rq.getObservacion() != null && rq.getObservacion().length() > 2000) {
            throw new BadRequestException("La observación no puede superar los 2000 caracteres");
        }
        if (rq.getDiagnosticoEvolucion() == null || rq.getDiagnosticoEvolucion().isBlank()
                || rq.getDiagnosticoEvolucion().length() > 2000) {
            throw new BadRequestException("El diagnóstico o evolución es obligatorio (máximo 2000 caracteres)");
        }
    }

    /**
     * Convierte el texto de búsqueda en un patrón LIKE en minúsculas (o nulo si está vacío).
     *
     * @param texto texto escrito por el usuario.
     * @return patrón con comodines, o {@code null}.
     */
    private String patronTexto(String texto) {
        return (texto == null || texto.isBlank()) ? null : "%" + texto.trim().toLowerCase() + "%";
    }

    /**
     * Busca una historia por id o lanza error 404.
     *
     * @param historiaId identificador de la historia.
     * @return la historia encontrada.
     */
    private HistoriaClinica buscarHistoriaOFallar(Long historiaId) {
        return this.historiaRepository.findById(historiaId)
                .orElseThrow(() -> new NotFoundException("La historia clínica no fue encontrada"));
    }

    private HistoriaResumenRs aResumen(HistoriaClinica h) {
        HistoriaResumenRs rs = new HistoriaResumenRs();
        rs.setHistoriaId(h.getHistoriaId());
        rs.setFechaHora(h.getFechaHora());
        rs.setMascota(h.getMascota().getNombreMascota());
        rs.setCliente(nombreCompleto(h.getMascota().getCliente().getNombres(),
                h.getMascota().getCliente().getApellidos()));
        rs.setVeterinario(nombreCompleto(h.getMedico().getNombres(), h.getMedico().getApellidos()));
        rs.setMotivo(h.getMotivo());
        rs.setEstado(h.getEstado());
        return rs;
    }

    private HistoriaDetalleRs aDetalle(HistoriaClinica h) {
        HistoriaDetalleRs rs = new HistoriaDetalleRs();
        rs.setHistoriaId(h.getHistoriaId());
        rs.setClienteId(h.getMascota().getCliente().getClienteId());
        rs.setCliente(nombreCompleto(h.getMascota().getCliente().getNombres(),
                h.getMascota().getCliente().getApellidos()));
        rs.setMascotaId(h.getMascota().getMascotaId());
        rs.setMascota(h.getMascota().getNombreMascota());
        rs.setCitaId(h.getCita().getCitaId());
        rs.setMedicoId(h.getMedico().getId());
        rs.setVeterinario(nombreCompleto(h.getMedico().getNombres(), h.getMedico().getApellidos()));
        rs.setFechaHora(h.getFechaHora());
        rs.setMotivo(h.getMotivo());
        rs.setDiagnostico(h.getDiagnostico());
        rs.setObservaciones(h.getObservaciones());
        rs.setEstado(h.getEstado());
        rs.setFechaCreacion(h.getFechaCreacion());
        return rs;
    }

    private AnotacionRs aAnotacionRs(Anotacion a) {
        AnotacionRs rs = new AnotacionRs();
        rs.setAnotacionId(a.getAnotacionId());
        rs.setHistoriaId(a.getHistoriaClinica().getHistoriaId());
        rs.setFechaHora(a.getFechaHora());
        rs.setMedicoId(a.getMedico().getId());
        rs.setVeterinario(nombreCompleto(a.getMedico().getNombres(), a.getMedico().getApellidos()));
        rs.setDescripcion(a.getDescripcion());
        rs.setObservacion(a.getObservacion());
        rs.setDiagnosticoEvolucion(a.getDiagnosticoEvolucion());
        return rs;
    }

    private static String nombreCompleto(String nombres, String apellidos) {
        return (nombres + " " + apellidos).trim();
    }

    private MiRespuestaRS construirRespuesta(int status, String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(status);
        rta.setMessage(mensaje);
        return rta;
    }
}
