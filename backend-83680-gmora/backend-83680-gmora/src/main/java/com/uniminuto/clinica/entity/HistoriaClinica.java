package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Historia clínica de una mascota, generada a partir de una cita atendida.
 * <p>
 * Relaciones (diagrama del parcial): Mascota 1:N Historia clínica; una historia
 * tiene N anotaciones y N fórmulas médicas. La llave única sobre {@code cita_id}
 * garantiza que una cita genere una sola historia (evita duplicidad).
 * Una historia nunca se elimina: es información clínica.
 */
@Entity
@Table(name = "historia_clinica")
@Data
public class HistoriaClinica {

    /** Estado inicial: admite nuevas anotaciones. */
    public static final String ABIERTA = "ABIERTA";

    /** Estado final: ya no admite anotaciones. */
    public static final String CERRADA = "CERRADA";

    /** Identificador único de la historia clínica. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long historiaId;

    /** Mascota a la que pertenece la historia (y, por ella, el cliente). */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Cita (consulta) que originó la historia. Única por historia. */
    @OneToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    /** Veterinario que realizó la consulta. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Fecha y hora de la consulta. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Motivo de la consulta. */
    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    /** Diagnóstico de la consulta. */
    @Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    /** Observaciones adicionales (opcional). */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /** Estado de la historia: ABIERTA o CERRADA. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    /** Fecha y hora en que se creó el registro. */
    @Column(name = "fecha_creacion_registro", nullable = false)
    private LocalDateTime fechaCreacion;
}
