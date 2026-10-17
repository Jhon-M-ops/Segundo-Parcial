export interface Cliente {
    clienteId: number;
    usuarioId: number | null;
    tipoDocumento: string;
    numeroDocumento: string;
    nombres: string;
    apellidos: string;
    fechaNacimiento: string;
    genero: string | null;
    telefono: string | null;
    direccion: string | null;
    activo: boolean;
}

export interface ClienteRq {
    clienteId: number | null;
    tipoDocumento: string;
    numeroDocumento: string;
    nombres: string;
    apellidos: string;
    fechaNacimiento: string;
    genero: string | null;
    telefono: string | null;
    direccion: string | null;
    activo: boolean;
}