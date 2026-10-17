package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.enums.EstadoCita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CitaDetalleRs;
import com.uniminuto.clinica.models.CitaEstadoRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaResumenRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementación de la gestión de citas. Aquí viven las reglas críticas de
 * negocio: el frontend solo orienta al usuario, la validación definitiva es esta.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Margen (en minutos) antes y después de una cita que se considera "mismo horario". */
    private static final long MARGEN_MINUTOS = 15;

    /** Valor de "excluir" cuando se crea una cita (ningún id coincide). */
    private static final long SIN_EXCLUIR = -1L;

    /** Tamaño máximo de página permitido. */
    private static final int TAMANO_MAXIMO_PAGINA = 100;

    /** Estados con los que se permite registrar una cita nueva. */
    private static final Set<EstadoCita> ESTADOS_INICIALES =
            EnumSet.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA, EstadoCita.PROGRAMADA);

    /**
     * Lista blanca de columnas ordenables: clave del frontend -> ruta en la entidad.
     * Fecha y hora ordenan por el mismo campo (fecha_hora).
     */
    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "fechaHora", "fechaHora",
            "fecha", "fechaHora",
            "hora", "fechaHora",
            "cliente", "mascota.cliente.nombres",
            "mascota", "mascota.nombreMascota",
            "veterinario", "medico.nombres",
            "estado", "estado");

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<CitaResumenRs> listarCitas(CitaFiltroRq filtro) {
        String campo = CAMPOS_ORDEN.getOrDefault(filtro.getSortBy(), "fechaHora");
        Sort.Direction direccion = "asc".equalsIgnoreCase(filtro.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        int tamano = Math.min(Math.max(filtro.getSize(), 1), TAMANO_MAXIMO_PAGINA);
        int pagina = Math.max(filtro.getPage(), 0);
        Sort orden = Sort.by(direccion, campo).and(Sort.by(Sort.Direction.DESC, "citaId"));
        Pageable pageable = PageRequest.of(pagina, tamano, orden);

        String texto = (filtro.getTexto() == null || filtro.getTexto().isBlank())
                ? null : "%" + filtro.getTexto().trim().toLowerCase() + "%";

        String estado = null;
        if (filtro.getEstado() != null && !filtro.getEstado().isBlank()) {
            estado = EstadoCita.desdeTexto(filtro.getEstado())
                    .orElseThrow(() -> new BadRequestException("El estado indicado en el filtro no es válido"))
                    .name();
        }

        LocalDateTime desde = filtro.getFechaDesde() == null ? null : filtro.getFechaDesde().atStartOfDay();
        LocalDateTime hasta = filtro.getFechaHasta() == null ? null : filtro.getFechaHasta().plusDays(1).atStartOfDay();

        Page<Cita> resultado = this.citaRepository.buscar(
                texto, filtro.getMedicoId(), estado, desde, hasta, pageable);

        return new PaginaRs<>(
                resultado.getContent().stream().map(this::aResumen).toList(),
                resultado.getNumber(), resultado.getSize(),
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CitaDetalleRs consultarCita(Long citaId) {
        return this.aDetalle(this.buscarOFallar(citaId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarCita(CitaRq rq) {
        this.validarCamposObligatorios(rq);

        EstadoCita estado = this.interpretarEstado(rq.getEstado());
        if (!ESTADOS_INICIALES.contains(estado)) {
            throw new BadRequestException(
                    "Una cita nueva solo puede registrarse como Pendiente, Confirmada o Programada");
        }

        // Regla 1: el cliente debe existir.
        Cliente cliente = this.clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no existe"));

        Mascota mascota = this.mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe"));

        // Regla 2: la mascota debe pertenecer al cliente seleccionado.
        if (mascota.getCliente() == null
                || !mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente");
        }

        // Regla 3: el veterinario debe existir.
        Medico medico = this.medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe"));

        LocalDateTime fechaHora = this.combinar(rq);

        // Regla 6: no se registran citas en el pasado.
        this.validarNoEsPasada(fechaHora);

        // Reglas 4 y 5: disponibilidad del veterinario y de la mascota.
        this.validarDisponibilidad(medico.getId(), mascota.getMascotaId(), fechaHora, SIN_EXCLUIR);

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(fechaHora);
        cita.setMotivo(rq.getMotivo().trim());
        cita.setEstado(estado.name());
        this.citaRepository.save(cita);

        return this.construirRespuesta(201, "Cita registrada correctamente");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Qué se puede modificar según el estado (y por qué):
     * <ul>
     *     <li>Pendiente / Programada: fecha, hora, veterinario y motivo. Aún no se ha
     *     comprometido nada con el cliente.</li>
     *     <li>Confirmada: solo el motivo. El cliente ya fue avisado de fecha, hora y
     *     veterinario; cambiarlos en silencio rompería la confirmación.</li>
     *     <li>Atendida: nada. Es información histórica que respalda la historia clínica
     *     y las fórmulas.</li>
     *     <li>Cancelada: nada. Solo puede reactivarse con la operación explícita.</li>
     *     <li>Cliente y mascota nunca cambian: identifican al paciente de la cita.</li>
     * </ul>
     */
    @Override
    @Transactional
    public MiRespuestaRS actualizarCita(Long citaId, CitaRq rq) {
        Cita cita = this.buscarOFallar(citaId);
        EstadoCita estadoActual = this.estadoDe(cita);

        // Regla 8: una cita cancelada no se modifica como si siguiera activa.
        if (estadoActual == EstadoCita.CANCELADA) {
            throw new ConflictException(
                    "Una cita cancelada no puede modificarse. Use la opción Reactivar si desea volver a activarla");
        }
        // Una cita atendida es información histórica.
        if (estadoActual == EstadoCita.ATENDIDA) {
            throw new ConflictException(
                    "Una cita atendida forma parte del historial clínico y no puede modificarse");
        }

        this.validarCamposObligatorios(rq, false);

        boolean cambioPaciente = !cita.getMascota().getMascotaId().equals(rq.getMascotaId())
                || !cita.getMascota().getCliente().getClienteId().equals(rq.getClienteId());
        if (cambioPaciente) {
            throw new ConflictException(
                    "El cliente y la mascota de una cita no pueden cambiarse. Cancele la cita y registre una nueva");
        }

        LocalDateTime nuevaFechaHora = this.combinar(rq);
        boolean cambioAgenda =
                !nuevaFechaHora.equals(cita.getFechaHora().truncatedTo(ChronoUnit.MINUTES))
                        || !cita.getMedico().getId().equals(rq.getMedicoId());

        if (cambioAgenda) {
            if (estadoActual == EstadoCita.CONFIRMADA) {
                throw new ConflictException(
                        "Una cita confirmada solo permite modificar el motivo. "
                                + "Para cambiar fecha, hora o veterinario cancélela y registre una nueva");
            }
            Medico medico = this.medicoRepository.findById(rq.getMedicoId())
                    .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe"));
            this.validarNoEsPasada(nuevaFechaHora);
            this.validarDisponibilidad(medico.getId(), cita.getMascota().getMascotaId(),
                    nuevaFechaHora, cita.getCitaId());
            cita.setMedico(medico);
            cita.setFechaHora(nuevaFechaHora);
        }

        cita.setMotivo(rq.getMotivo().trim());
        this.citaRepository.save(cita);
        return this.construirRespuesta(200, "Cita actualizada correctamente");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS cambiarEstado(Long citaId, CitaEstadoRq rq) {
        Cita cita = this.buscarOFallar(citaId);
        if (rq == null || rq.getEstado() == null || rq.getEstado().isBlank()) {
            throw new BadRequestException("El estado destino es obligatorio");
        }
        EstadoCita destino = this.interpretarEstado(rq.getEstado());
        EstadoCita actual = this.estadoDe(cita);

        if (!actual.puedePasarA(destino)) {
            throw new ConflictException("No es posible cambiar una cita de "
                    + actual.getEtiqueta() + " a " + destino.getEtiqueta());
        }
        // No se puede atender una cita que aún no ha llegado.
        if (destino == EstadoCita.ATENDIDA && cita.getFechaHora().toLocalDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("No se puede marcar como atendida una cita con fecha futura");
        }
        cita.setEstado(destino.name());
        this.citaRepository.save(cita);
        return this.construirRespuesta(200, "La cita ahora está " + destino.getEtiqueta());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS reactivarCita(Long citaId) {
        Cita cita = this.buscarOFallar(citaId);
        if (this.estadoDe(cita) != EstadoCita.CANCELADA) {
            throw new ConflictException("Solo se pueden reactivar las citas canceladas");
        }
        // Al reactivar se vuelven a validar fecha y disponibilidad (pudieron cambiar).
        this.validarNoEsPasada(cita.getFechaHora());
        this.validarDisponibilidad(cita.getMedico().getId(), cita.getMascota().getMascotaId(),
                cita.getFechaHora(), cita.getCitaId());
        cita.setEstado(EstadoCita.PENDIENTE.name());
        this.citaRepository.save(cita);
        return this.construirRespuesta(200, "La cita fue reactivada y quedó Pendiente");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorMascota(Integer mascotaId) {
        if (mascotaId == null) {
            throw new BadRequestException("El ID de la mascota no puede ser nulo");
        }
        Mascota mascota = this.mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new NotFoundException("Mascota no encontrada"));
        return this.citaRepository.findAllByMascotaOrderByFechaHoraDesc(mascota);
    }

    // =====================================================================
    // Validaciones y utilidades privadas
    // =====================================================================

    /**
     * Valida los campos obligatorios al crear (incluye el estado).
     *
     * @param rq datos recibidos.
     */
    private void validarCamposObligatorios(CitaRq rq) {
        this.validarCamposObligatorios(rq, true);
    }

    /**
     * Valida que los campos obligatorios estén diligenciados.
     *
     * @param rq            datos recibidos.
     * @param exigirEstado  {@code true} al crear (el estado inicial es obligatorio).
     */
    private void validarCamposObligatorios(CitaRq rq, boolean exigirEstado) {
        if (rq == null) {
            throw new BadRequestException("Los datos de la cita son obligatorios");
        }
        if (rq.getClienteId() == null) {
            throw new BadRequestException("El cliente es obligatorio");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio");
        }
        if (rq.getFecha() == null) {
            throw new BadRequestException("La fecha es obligatoria");
        }
        if (rq.getHora() == null) {
            throw new BadRequestException("La hora es obligatoria");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank() || rq.getMotivo().length() > 255) {
            throw new BadRequestException("El motivo de la consulta es obligatorio (máximo 255 caracteres)");
        }
        if (exigirEstado && (rq.getEstado() == null || rq.getEstado().isBlank())) {
            throw new BadRequestException("El estado es obligatorio");
        }
    }

    /**
     * Convierte un texto en {@link EstadoCita} o lanza error 400.
     *
     * @param texto estado recibido.
     * @return estado válido.
     */
    private EstadoCita interpretarEstado(String texto) {
        return EstadoCita.desdeTexto(texto).orElseThrow(() -> new BadRequestException(
                "El estado no es válido. Use: Pendiente, Confirmada, Atendida, Cancelada o Programada"));
    }

    /**
     * Obtiene el estado de una cita ya guardada.
     *
     * @param cita cita almacenada.
     * @return su estado.
     */
    private EstadoCita estadoDe(Cita cita) {
        return EstadoCita.desdeTexto(cita.getEstado())
                .orElseThrow(() -> new ConflictException("La cita tiene un estado no reconocido"));
    }

    /**
     * Une fecha y hora (a precisión de minutos).
     *
     * @param rq datos recibidos.
     * @return fecha y hora de la cita.
     */
    private LocalDateTime combinar(CitaRq rq) {
        return rq.getFecha().atTime(rq.getHora()).truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Regla 6: no se pueden registrar ni mover citas a fechas/horas anteriores a la actual.
     *
     * @param fechaHora fecha y hora propuestas.
     */
    private void validarNoEsPasada(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas anteriores a la fecha actual");
        }
    }

    /**
     * Reglas 4 y 5: ni el veterinario ni la mascota pueden tener otra cita (no
     * cancelada) dentro de los 15 minutos anteriores o posteriores.
     *
     * @param medicoId  veterinario.
     * @param mascotaId mascota.
     * @param fechaHora fecha y hora propuestas.
     * @param excluirId cita a ignorar (la que se edita) o {@link #SIN_EXCLUIR}.
     */
    private void validarDisponibilidad(Long medicoId, Integer mascotaId, LocalDateTime fechaHora, Long excluirId) {
        LocalDateTime desde = fechaHora.minusMinutes(MARGEN_MINUTOS);
        LocalDateTime hasta = fechaHora.plusMinutes(MARGEN_MINUTOS);

        if (this.citaRepository.contarCitasDelMedicoEnRango(medicoId, desde, hasta, excluirId) > 0) {
            throw new ConflictException("El veterinario no está disponible en ese horario: "
                    + "ya tiene una cita dentro de los " + MARGEN_MINUTOS + " minutos anteriores o posteriores");
        }
        if (this.citaRepository.contarCitasDeMascotaEnRango(mascotaId, desde, hasta, excluirId) > 0) {
            throw new ConflictException("La mascota ya tiene una cita en ese horario (cita duplicada)");
        }
    }

    /**
     * Busca una cita por id o lanza error 404.
     *
     * @param citaId identificador de la cita.
     * @return la cita encontrada.
     */
    private Cita buscarOFallar(Long citaId) {
        return this.citaRepository.findById(citaId)
                .orElseThrow(() -> new NotFoundException("La cita no fue encontrada"));
    }

    /**
     * Convierte la entidad en el resumen del listado.
     *
     * @param c cita.
     * @return resumen.
     */
    private CitaResumenRs aResumen(Cita c) {
        CitaResumenRs rs = new CitaResumenRs();
        rs.setCitaId(c.getCitaId());
        rs.setFechaHora(c.getFechaHora());
        rs.setCliente(nombreCompleto(c.getMascota().getCliente().getNombres(),
                c.getMascota().getCliente().getApellidos()));
        rs.setMascota(c.getMascota().getNombreMascota());
        rs.setVeterinario(nombreCompleto(c.getMedico().getNombres(), c.getMedico().getApellidos()));
        rs.setEstado(c.getEstado());
        return rs;
    }

    /**
     * Convierte la entidad en el detalle completo.
     *
     * @param c cita.
     * @return detalle.
     */
    private CitaDetalleRs aDetalle(Cita c) {
        CitaDetalleRs rs = new CitaDetalleRs();
        rs.setCitaId(c.getCitaId());
        rs.setClienteId(c.getMascota().getCliente().getClienteId());
        rs.setCliente(nombreCompleto(c.getMascota().getCliente().getNombres(),
                c.getMascota().getCliente().getApellidos()));
        rs.setMascotaId(c.getMascota().getMascotaId());
        rs.setMascota(c.getMascota().getNombreMascota());
        rs.setMedicoId(c.getMedico().getId());
        rs.setVeterinario(nombreCompleto(c.getMedico().getNombres(), c.getMedico().getApellidos()));
        rs.setFechaHora(c.getFechaHora());
        rs.setMotivo(c.getMotivo());
        rs.setEstado(c.getEstado());
        return rs;
    }

    /**
     * Une nombres y apellidos.
     *
     * @param nombres   nombres.
     * @param apellidos apellidos.
     * @return nombre completo.
     */
    private static String nombreCompleto(String nombres, String apellidos) {
        return (nombres + " " + apellidos).trim();
    }

    /**
     * Construye la respuesta estándar de éxito.
     *
     * @param status  código lógico.
     * @param mensaje mensaje para el usuario.
     * @return respuesta.
     */
    private MiRespuestaRS construirRespuesta(int status, String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(status);
        rta.setMessage(mensaje);
        return rta;
    }
}
