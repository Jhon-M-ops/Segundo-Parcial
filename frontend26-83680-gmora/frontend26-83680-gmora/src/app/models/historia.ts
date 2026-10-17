/** Estados de una historia clínica. */
export type EstadoHistoria = 'ABIERTA' | 'CERRADA';

/** Estados con etiqueta legible, para filtros. */
export const ESTADOS_HISTORIA: { valor: EstadoHistoria; etiqueta: string }[] = [
    { valor: 'ABIERTA', etiqueta: 'Abierta' },
    { valor: 'CERRADA', etiqueta: 'Cerrada' }
];

/** Información resumida de una historia (listado). */
export interface HistoriaResumen {
    historiaId: number;
    fechaHora: string;
    mascota: string;
    cliente: string;
    veterinario: string;
    motivo: string;
    estado: string;
}

/** Información completa de una historia (vista "Ver"). */
export interface HistoriaDetalle extends HistoriaResumen {
    clienteId: number;
    mascotaId: number;
    citaId: number;
    medicoId: number;
    diagnostico: string;
    observaciones: string | null;
    fechaCreacion: string;
}

/** Datos que se envían para crear una historia. */
export interface HistoriaRq {
    clienteId: number;
    mascotaId: number;
    citaId: number;
    medicoId: number;
    fechaHora: string;
    motivo: string;
    diagnostico: string;
    observaciones: string;
}

/** Parámetros del listado de historias: paginación, orden y filtros. */
export interface HistoriaFiltro {
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

/** Anotación de una historia clínica. */
export interface Anotacion {
    anotacionId: number;
    historiaId: number;
    fechaHora: string;
    medicoId: number;
    veterinario: string;
    descripcion: string;
    observacion: string | null;
    diagnosticoEvolucion: string;
}

/** Datos que se envían para agregar una anotación (la fecha la pone el servidor). */
export interface AnotacionRq {
    medicoId: number;
    descripcion: string;
    observacion: string;
    diagnosticoEvolucion: string;
}

/** Parámetros del histórico de anotaciones. */
export interface AnotacionFiltro {
    page: number;
    size: number;
    sortDir: 'asc' | 'desc';
    texto?: string | null;
    fechaDesde?: string | null;
    fechaHasta?: string | null;
}
