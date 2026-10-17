import { Cliente } from "./cliente";
import { Raza } from "./raza";

export class Mascota {
    mascotaId?: number;
    nombreMascota?: string;
    edad?: number;
    raza?: Raza;
    fechaRegistro?: string;
    fechaModificacion?: string | null;
    cliente?: Cliente;
}

export interface MascotaRq {
    mascotaId: number | null;
    nombreMascota: string;
    edad: number;
    razaId: number;
    clienteId: number;
}