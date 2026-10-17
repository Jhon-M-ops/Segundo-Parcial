export class Raza {
    razaId?: number;
    nombre?: string;
    especie?: string;
    fechaCreacion?: string;
    fechaModificacion?: string | null;
}

export interface RazaRq {
    razaId: number | null;
    nombre: string;
    especie: string;
}