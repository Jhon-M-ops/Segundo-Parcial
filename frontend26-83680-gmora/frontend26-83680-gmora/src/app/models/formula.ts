/**
 * Información resumida de una fórmula. Es lo único que muestra el listado.
 */
export interface FormulaResumen {
    formulaId: number;
    fechaCreacion: string;
    fechaEmision: string;
    mascota: string;
    cliente: string;
    veterinario: string;
    medicamento: string;
    activo: boolean;
}

/**
 * Información completa de una fórmula. Se usa en la vista "Ver" y para
 * precargar el formulario de edición.
 */
export interface FormulaDetalle extends FormulaResumen {
    mascotaId: number;
    clienteId: number;
    historiaClinicaId: number | null;
    citaId: number;
    citaFechaHora: string;
    citaMotivo: string;
    medicoId: number;
    medicamentoId: number;
    dosis: number;
    unidadDosis: string;
    frecuencia: string;
    duracionDias: number;
    indicaciones: string;
    fechaModificacion: string | null;
}

/**
 * Datos que se envían al backend para crear o editar una fórmula.
 */
export interface FormulaRq {
    mascotaId: number;
    citaId: number;
    medicoId: number;
    medicamentoId: number;
    fechaEmision: string;
    dosis: number;
    unidadDosis: string;
    frecuencia: string;
    duracionDias: number;
    indicaciones: string;
}

/**
 * Parámetros del listado: paginación, orden y filtros.
 */
export interface FormulaFiltro {
    page: number;
    size: number;
    sortBy: string;
    sortDir: 'asc' | 'desc';
    texto?: string | null;
    medicoId?: number | null;
    medicamentoId?: number | null;
    activo?: boolean | string | null;
    fechaDesde?: string | null;
    fechaHasta?: string | null;
}
