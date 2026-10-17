package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fórmula médica emitida por un veterinario para una mascota en una consulta.
 * <p>
 * No se elimina físicamente: si ya está asociada a información clínica se
 * inactiva mediante el campo {@code activo} (regla de negocio 8).
 */
@Entity
@Table(name = "formula_medica")
@Data
public class FormulaMedica {

    /** Identificador único de la fórmula. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long formulaId;

    /** Mascota a la que se le formula. */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Consulta (cita) en la que se emitió la fórmula. */
    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    /** Veterinario que emite la fórmula. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Medicamento formulado. */
    @ManyToOne
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    /** Fecha de emisión (no puede ser anterior a la fecha de la consulta). */
    /**
     * Historia clínica a la que pertenece la fórmula (Historia 1:N Fórmula). Es opcional
     * porque la fórmula puede emitirse antes de que exista la historia; se enlaza
     * automáticamente cuando la consulta ya tiene historia.
     */
    @ManyToOne
    @JoinColumn(name = "historia_id")
    private HistoriaClinica historiaClinica;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /** Cantidad de la dosis (mayor que cero). */
    @Column(name = "dosis_numero", precision = 10, scale = 2)
    private BigDecimal dosis;

    /** Texto de dosis conservado para compatibilidad con los registros existentes. */
    @Column(name = "dosis", nullable = false, columnDefinition = "TEXT")
    private String dosisTexto;

    /** Unidad de la dosis (mg, ml, tableta, gotas...). */
    @Column(name = "unidad_dosis", nullable = false, length = 20)
    private String unidadDosis;

    /** Frecuencia de administración (ej: "Cada 8 horas"). */
    @Column(name = "frecuencia", nullable = false, length = 100)
    private String frecuencia;

    /** Duración del tratamiento en días. */
    @Column(name = "duracion_dias")
    private Integer duracionDias;

    /** Indicaciones adicionales para el propietario. */
    @Column(name = "indicaciones", nullable = false, columnDefinition = "TEXT")
    private String indicaciones;

    /** Indica si la fórmula está vigente (true) o inactiva (false). */
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /** Fecha y hora en que se creó el registro. */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha y hora de la última modificación. */
    @Column(name = "fecha_actualizacion_registro")
    private LocalDateTime fechaModificacion;
}
