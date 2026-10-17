package com.uniminuto.clinica.enums;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Estados posibles de una cita y su máquina de transiciones.
 * <p>
 * El estado se persiste como texto en mayúsculas (columna {@code estado} de
 * la tabla {@code cita}). Las transiciones válidas viven aquí para que la regla
 * sea única y la use el servicio (el backend es quien decide).
 */
public enum EstadoCita {

    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    PROGRAMADA("Programada");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return nombre legible del estado para mensajes al usuario.
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Estados a los que puede pasar una cita desde este estado.
     * <ul>
     *     <li>Pendiente  → Confirmada, Programada, Cancelada.</li>
     *     <li>Programada → Confirmada, Atendida, Cancelada.</li>
     *     <li>Confirmada → Atendida, Cancelada.</li>
     *     <li>Atendida   → ninguno (forma parte del historial clínico).</li>
     *     <li>Cancelada  → ninguno (solo se reactiva con la operación explícita).</li>
     * </ul>
     *
     * @return conjunto de destinos permitidos.
     */
    public Set<EstadoCita> destinosPermitidos() {
        return switch (this) {
            case PENDIENTE -> EnumSet.of(CONFIRMADA, PROGRAMADA, CANCELADA);
            case PROGRAMADA -> EnumSet.of(CONFIRMADA, ATENDIDA, CANCELADA);
            case CONFIRMADA -> EnumSet.of(ATENDIDA, CANCELADA);
            case ATENDIDA, CANCELADA -> EnumSet.noneOf(EstadoCita.class);
        };
    }

    /**
     * Indica si es válido pasar de este estado al destino.
     *
     * @param destino estado al que se quiere pasar.
     * @return {@code true} si la transición está permitida.
     */
    public boolean puedePasarA(EstadoCita destino) {
        return destinosPermitidos().contains(destino);
    }

    /**
     * Convierte un texto en estado, sin distinguir mayúsculas/minúsculas.
     *
     * @param texto texto recibido (por ejemplo "pendiente").
     * @return el estado, o vacío si el texto no corresponde a ninguno.
     */
    public static Optional<EstadoCita> desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        for (EstadoCita estado : values()) {
            if (estado.name().equalsIgnoreCase(texto.trim())) {
                return Optional.of(estado);
            }
        }
        return Optional.empty();
    }
}
