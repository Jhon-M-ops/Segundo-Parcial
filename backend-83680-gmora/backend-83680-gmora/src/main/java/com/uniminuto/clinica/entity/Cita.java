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

import java.time.LocalDateTime;

/**
 * Cita médica de una mascota con un veterinario.
 * <p>
 * Una cita pertenece a una mascota (y por ella a un cliente) y la atiende un
 * veterinario. Las fórmulas médicas y las historias clínicas se asocian a ella.
 */
@Entity
@Table(name = "cita")
@Data
public class Cita {

    /** Identificador único de la cita. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long citaId;

    /** Cliente asociado a la cita. */
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Mascota que será atendida. */
    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** Veterinario asignado a la cita. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Fecha y hora programada de la cita. */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Motivo de la consulta. */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /** Estado: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA o PROGRAMADA (ver EstadoCita). */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

}
