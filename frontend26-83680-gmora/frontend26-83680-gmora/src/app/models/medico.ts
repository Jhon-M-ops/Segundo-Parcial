import { Especializacion } from "./especializacion";

export class Medico {
    id?: number;
    tipoDocumento?: string;
    numeroDocumento?: string;
    nombres?: string;
    apellidos?: string;
    telefono?: string | null;
    registroProfesional?: string;
    especializacion?: Especializacion;
}

export interface MedicoRq {
    id: number | null;
    tipoDocumento: string;
    numeroDocumento: string;
    nombres: string;
    apellidos: string;
    telefono: string | null;
    registroProfesional: string;
    especializacionId: number;
}