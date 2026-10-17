export class Medicamento {
    id?: number;
    nombre?: string;
    descripcion?: string | null;
    presentacion?: string | null;
    fechaCompra?: string | null;
    fechaVence?: string | null;
    fechaCreacionRegistro?: string | null;
    fechaModificacionRegistro?: string | null;
}

export interface MedicamentoRq {
    id: number | null;
    nombre: string;
    descripcion: string;
    presentacion: string;
    fechaCompra: string | null;
    fechaVence: string | null;
}