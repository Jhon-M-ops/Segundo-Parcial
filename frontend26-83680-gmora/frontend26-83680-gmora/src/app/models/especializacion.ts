export class Especializacion {
    id?: number;
    nombre?: string;
    descripcion?: string | null;
    codigoEspecializacion?: string;
}

export interface EspecializacionRq {
    id: number | null;
    nombre: string;
    descripcion: string;
    codigoEspecializacion: string;
}