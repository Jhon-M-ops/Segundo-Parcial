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
 * Anotación de seguimiento dentro de una historia clínica.
 * <p>
 * Relación: Historia clínica 1:N Anotación. La columna {@code historia_id} es
 * obligatoria: no puede existir una anotación sin historia (sin información huérfana).
 */
@Entity
@Table(name = "anotacion")
@Data
public class Anotacion {

    /** Identificador único de la anotación. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long anotacionId;

    /** Historia clínica a la que pertenece. */
    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaClinica historiaClinica;

    /** Veterinario que escribe la anotación. */
    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    /** Fecha y hora de la anotación (la asigna el servidor). */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Descripción de lo ocurrido. */
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /** Observación adicional (opcional). */
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    /** Diagnóstico o evolución del paciente. */
    @Column(name = "diagnostico_evolucion", nullable = false, columnDefinition = "TEXT")
    private String diagnosticoEvolucion;
}
