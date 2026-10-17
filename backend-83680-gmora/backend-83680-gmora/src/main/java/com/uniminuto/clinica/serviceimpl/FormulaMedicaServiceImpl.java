package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaClinica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaFiltroRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaClinicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementación de la gestión de fórmulas médicas. Aquí viven las reglas de
 * negocio: el frontend solo ayuda al usuario, pero la validación definitiva
 * se hace en esta clase.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Tamaño máximo de página permitido. */
    private static final int TAMANO_MAXIMO_PAGINA = 100;

    /**
     * Lista blanca de columnas ordenables: clave enviada por el frontend ->
     * ruta real en la entidad. Evita ordenar por campos arbitrarios.
     */
    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "fechaCreacion", "fechaCreacion",
            "fechaEmision", "fechaEmision",
            "mascota", "mascota.nombreMascota",
            "cliente", "mascota.cliente.nombres",
            "veterinario", "medico.nombres",
            "medicamento", "medicamento.nombre",
            "activo", "activo");

    @Autowired
    private FormulaMedicaRepository formulaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HistoriaClinicaRepository historiaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<FormulaResumenRs> listarFormulas(FormulaFiltroRq filtro) {
        String campo = CAMPOS_ORDEN.getOrDefault(filtro.getSortBy(), "fechaCreacion");
        Sort.Direction direccion = "asc".equalsIgnoreCase(filtro.getSortDir())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        int tamano = Math.min(Math.max(filtro.getSize(), 1), TAMANO_MAXIMO_PAGINA);
        int pagina = Math.max(filtro.getPage(), 0);

        // El segundo criterio (id) hace estable la paginación cuando hay empates.
        Sort orden = Sort.by(direccion, campo).and(Sort.by(Sort.Direction.DESC, "formulaId"));
        Pageable pageable = PageRequest.of(pagina, tamano, orden);

        String texto = (filtro.getTexto() == null || filtro.getTexto().isBlank())
                ? null
                : "%" + filtro.getTexto().trim().toLowerCase() + "%";

        Page<FormulaMedica> resultado = this.formulaRepository.buscar(
                texto, filtro.getMedicoId(), filtro.getMedicamentoId(), filtro.getActivo(),
                filtro.getFechaDesde(), filtro.getFechaHasta(), pageable);

        return new PaginaRs<>(
                resultado.getContent().stream().map(this::aResumen).toList(),
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public FormulaDetalleRs consultarFormula(Long formulaId) {
        return this.aDetalle(this.buscarOFallar(formulaId));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarFormula(FormulaRq formulaRq) {
        FormulaMedica formula = new FormulaMedica();
        this.validarYAsignar(formula, formulaRq);
        formula.setActivo(true);
        formula.setFechaCreacion(LocalDateTime.now());
        this.formulaRepository.save(formula);
        return this.construirRespuesta(201, "Fórmula médica registrada correctamente");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarFormula(Long formulaId, FormulaRq formulaRq) {
        FormulaMedica formula = this.buscarOFallar(formulaId);

        if (!Boolean.TRUE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula está inactiva y no puede modificarse");
        }
        if (formulaRq == null) {
            throw new BadRequestException("Los datos de la fórmula son obligatorios");
        }

        // Campos inmutables: impiden reasignar la fórmula a otra mascota,
        // consulta o veterinario (regla 7 también en la edición).
        boolean cambioIdentidad =
                !formula.getMascota().getMascotaId().equals(formulaRq.getMascotaId())
                        || !formula.getCita().getCitaId().equals(formulaRq.getCitaId())
                        || !formula.getMedico().getId().equals(formulaRq.getMedicoId());
        if (cambioIdentidad) {
            throw new ConflictException(
                    "La mascota, la consulta y el veterinario de una fórmula emitida no se pueden cambiar");
        }

        this.validarYAsignar(formula, formulaRq);
        formula.setFechaModificacion(LocalDateTime.now());
        this.formulaRepository.save(formula);
        return this.construirRespuesta(200, "Fórmula médica actualizada correctamente");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS inactivarFormula(Long formulaId) {
        FormulaMedica formula = this.buscarOFallar(formulaId);
        if (!Boolean.TRUE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula ya se encuentra inactiva");
        }
        formula.setActivo(false);
        formula.setFechaModificacion(LocalDateTime.now());
        this.formulaRepository.save(formula);
        return this.construirRespuesta(200, "Fórmula inactivada correctamente");
    }

    /**
     * Valida todas las reglas de negocio y copia los datos a la entidad.
     *
     * @param formula entidad destino (nueva o existente).
     * @param rq      datos recibidos.
     */
    private void validarYAsignar(FormulaMedica formula, FormulaRq rq) {
        // Regla 6: campos obligatorios y valores válidos (regla 5).
        this.validarCamposObligatorios(rq);

        // Regla 1: la mascota debe existir.
        Mascota mascota = this.mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe"));

        // Regla 2: la mascota debe pertenecer a un cliente registrado.
        if (mascota.getCliente() == null) {
            throw new BadRequestException("La mascota no pertenece a un cliente registrado");
        }

        // Regla 3: el veterinario debe existir.
        Medico medico = this.medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe"));

        Cita cita = this.citaRepository.findById(rq.getCitaId())
                .orElseThrow(() -> new BadRequestException("La consulta seleccionada no existe"));

        // Regla 7: la consulta debe ser de la misma mascota.
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La consulta seleccionada pertenece a otra mascota");
        }

        // Una cita cancelada no tuvo consulta, por lo tanto no admite fórmulas.
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado())) {
            throw new BadRequestException("No se puede emitir una fórmula para una cita cancelada");
        }

        // Regla 4: la emisión no puede ser anterior a la fecha de la consulta.
        if (rq.getFechaEmision().isBefore(cita.getFechaHora().toLocalDate())) {
            throw new BadRequestException(
                    "La fecha de emisión no puede ser anterior a la fecha de la consulta");
        }

        Medicamento medicamento = this.medicamentoRepository.findById(rq.getMedicamentoId())
                .orElseThrow(() -> new BadRequestException("El medicamento seleccionado no existe"));

        // Evita duplicidad: una misma consulta no puede tener dos fórmulas activas del mismo medicamento.
        boolean duplicada = formula.getFormulaId() == null
                ? this.formulaRepository.existsByCitaAndMedicamentoAndActivoTrue(cita, medicamento)
                : this.formulaRepository.existsByCitaAndMedicamentoAndActivoTrueAndFormulaIdNot(
                        cita, medicamento, formula.getFormulaId());
        if (duplicada) {
            throw new ConflictException(
                    "Ya existe una fórmula activa de este medicamento para la misma consulta");
        }

        // Relación Historia 1:N Fórmula: si la consulta ya tiene historia, la fórmula se enlaza a ella.
        HistoriaClinica historia = this.historiaRepository.findByCita(cita).orElse(null);
        if (historia != null && !historia.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new ConflictException("La historia clínica de la consulta pertenece a otra mascota");
        }

        formula.setMascota(mascota);
        formula.setCita(cita);
        formula.setHistoriaClinica(historia);
        formula.setMedico(medico);
        formula.setMedicamento(medicamento);
        formula.setFechaEmision(rq.getFechaEmision());
        formula.setDosis(rq.getDosis());
        formula.setDosisTexto(rq.getDosis().stripTrailingZeros().toPlainString()
                + " " + rq.getUnidadDosis().trim()
                + " " + rq.getFrecuencia().trim()
                + " durante " + rq.getDuracionDias() + " días");
        formula.setUnidadDosis(rq.getUnidadDosis().trim());
        formula.setFrecuencia(rq.getFrecuencia().trim());
        formula.setDuracionDias(rq.getDuracionDias());
        formula.setIndicaciones(rq.getIndicaciones().trim());
    }

    /**
     * Valida que los campos obligatorios estén diligenciados y tengan valores válidos.
     *
     * @param rq datos recibidos.
     */
    private void validarCamposObligatorios(FormulaRq rq) {
        if (rq == null) {
            throw new BadRequestException("Los datos de la fórmula son obligatorios");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria");
        }
        if (rq.getCitaId() == null) {
            throw new BadRequestException("La consulta es obligatoria");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio");
        }
        if (rq.getMedicamentoId() == null) {
            throw new BadRequestException("El medicamento es obligatorio");
        }
        if (rq.getFechaEmision() == null) {
            throw new BadRequestException("La fecha de emisión es obligatoria");
        }
        // Regla 5: dosis y duración válidas.
        if (rq.getDosis() == null || rq.getDosis().signum() <= 0) {
            throw new BadRequestException("La dosis debe ser un número mayor que cero");
        }
        if (rq.getUnidadDosis() == null || rq.getUnidadDosis().isBlank()) {
            throw new BadRequestException("La unidad de la dosis es obligatoria");
        }
        if (rq.getFrecuencia() == null || rq.getFrecuencia().isBlank()) {
            throw new BadRequestException("La frecuencia es obligatoria");
        }
        if (rq.getDuracionDias() == null || rq.getDuracionDias() < 1 || rq.getDuracionDias() > 365) {
            throw new BadRequestException("La duración del tratamiento debe estar entre 1 y 365 días");
        }
        if (rq.getIndicaciones() == null || rq.getIndicaciones().isBlank()
                || rq.getIndicaciones().length() > 1000) {
            throw new BadRequestException("Las indicaciones son obligatorias (máximo 1000 caracteres)");
        }
    }

    /**
     * Busca una fórmula por id o lanza un error 404.
     *
     * @param formulaId identificador de la fórmula.
     * @return la fórmula encontrada.
     */
    private FormulaMedica buscarOFallar(Long formulaId) {
        return this.formulaRepository.findById(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula médica no fue encontrada"));
    }

    /**
     * Convierte una entidad en el resumen que se muestra en el listado.
     *
     * @param f entidad.
     * @return resumen de la fórmula.
     */
    private FormulaResumenRs aResumen(FormulaMedica f) {
        FormulaResumenRs rs = new FormulaResumenRs();
        rs.setFormulaId(f.getFormulaId());
        rs.setFechaCreacion(f.getFechaCreacion());
        rs.setFechaEmision(f.getFechaEmision());
        rs.setMascota(f.getMascota().getNombreMascota());
        rs.setCliente(nombreCompleto(f.getMascota().getCliente().getNombres(),
                f.getMascota().getCliente().getApellidos()));
        rs.setVeterinario(nombreCompleto(f.getMedico().getNombres(), f.getMedico().getApellidos()));
        rs.setMedicamento(f.getMedicamento().getNombre());
        rs.setActivo(f.getActivo());
        return rs;
    }

    /**
     * Convierte una entidad en el detalle completo de la fórmula.
     *
     * @param f entidad.
     * @return detalle de la fórmula.
     */
    private FormulaDetalleRs aDetalle(FormulaMedica f) {
        FormulaDetalleRs rs = new FormulaDetalleRs();
        rs.setFormulaId(f.getFormulaId());
        rs.setMascotaId(f.getMascota().getMascotaId());
        rs.setMascota(f.getMascota().getNombreMascota());
        rs.setClienteId(f.getMascota().getCliente().getClienteId());
        rs.setCliente(nombreCompleto(f.getMascota().getCliente().getNombres(),
                f.getMascota().getCliente().getApellidos()));
        rs.setHistoriaClinicaId(f.getHistoriaClinica() == null ? null : f.getHistoriaClinica().getHistoriaId());
        rs.setCitaId(f.getCita().getCitaId());
        rs.setCitaFechaHora(f.getCita().getFechaHora());
        rs.setCitaMotivo(f.getCita().getMotivo());
        rs.setMedicoId(f.getMedico().getId());
        rs.setVeterinario(nombreCompleto(f.getMedico().getNombres(), f.getMedico().getApellidos()));
        rs.setMedicamentoId(f.getMedicamento().getId());
        rs.setMedicamento(f.getMedicamento().getNombre());
        rs.setFechaEmision(f.getFechaEmision());
        rs.setDosis(f.getDosis());
        rs.setUnidadDosis(f.getUnidadDosis());
        rs.setFrecuencia(f.getFrecuencia());
        rs.setDuracionDias(f.getDuracionDias());
        rs.setIndicaciones(f.getIndicaciones());
        rs.setActivo(f.getActivo());
        rs.setFechaCreacion(f.getFechaCreacion());
        rs.setFechaModificacion(f.getFechaModificacion());
        return rs;
    }

    /**
     * Une nombres y apellidos en un solo texto.
     *
     * @param nombres   nombres de la persona.
     * @param apellidos apellidos de la persona.
     * @return nombre completo.
     */
    private static String nombreCompleto(String nombres, String apellidos) {
        return (nombres + " " + apellidos).trim();
    }

    /**
     * Construye la respuesta estándar de éxito.
     *
     * @param status  código HTTP lógico.
     * @param mensaje mensaje para el usuario.
     * @return respuesta de éxito.
     */
    private MiRespuestaRS construirRespuesta(int status, String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(status);
        rta.setMessage(mensaje);
        return rta;
    }
}
