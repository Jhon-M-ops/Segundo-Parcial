import { Mascota } from './mascota';
import { Medico } from './medico';

/** Estados posibles de una cita (se guardan en mayúsculas en el backend). */
export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA' | 'PROGRAMADA';

/** Estados con etiqueta legible, para combos y filtros. */
export const ESTADOS_CITA: { valor: EstadoCita; etiqueta: string }[] = [
    { valor: 'PENDIENTE', etiqueta: 'Pendiente' },
    { valor: 'CONFIRMADA', etiqueta: 'Confirmada' },
    { valor: 'PROGRAMADA', etiqueta: 'Programada' },
    { valor: 'ATENDIDA', etiqueta: 'Atendida' },
    { valor: 'CANCELADA', etiqueta: 'Cancelada' }
];

/** Estados con los que se puede registrar una cita nueva. */
export const ESTADOS_INICIALES_CITA: EstadoCita[] = ['PENDIENTE', 'CONFIRMADA', 'PROGRAMADA'];

/**
 * Transiciones de estado permitidas. Es un espejo de la regla del backend
 * (EstadoCita.java) y solo sirve para mostrar u ocultar botones: la validación
 * real la hace el backend.
 */
export const TRANSICIONES_CITA: Record<EstadoCita, EstadoCita[]> = {
    PENDIENTE: ['CONFIRMADA', 'PROGRAMADA', 'CANCELADA'],
    PROGRAMADA: ['CONFIRMADA', 'ATENDIDA', 'CANCELADA'],
    CONFIRMADA: ['ATENDIDA', 'CANCELADA'],
    ATENDIDA: [],
    CANCELADA: []
};

/**
 * Cita tal como la devuelve el combo de citas por mascota
 * (usado por el módulo de fórmulas).
 */
export interface Cita {
    citaId: number;
    mascota: Mascota;
    medico: Medico;
    fechaHora: string;
    motivo: string;
    estado: string;
}

/** Información resumida de una cita (listado). */
export interface CitaResumen {
    citaId: number;
    fechaHora: string;
    cliente: string;
    mascota: string;
    veterinario: string;
    estado: string;
}

/** Información completa de una cita (Ver / precarga de edición). */
export interface CitaDetalle extends CitaResumen {
    clienteId: number;
    mascotaId: number;
    medicoId: number;
    motivo: string;
}

/** Datos que se envían para crear o editar una cita. */
export interface CitaRq {
    clienteId: number;
    mascotaId: number;
    medicoId: number;
    fecha: string;
    hora: string;
    motivo: string;
    estado: string;
}

/** Parámetros del listado: paginación, orden y filtros. */
export interface CitaFiltro {
    page: number;
    size: number;
    sortBy: string;
    sortDir: 'asc' | 'desc';
    texto?: string | null;
    medicoId?: number | null;
    estado?: string | null;
    fechaDesde?: string | null;
    fechaHasta?: string | null;
}
