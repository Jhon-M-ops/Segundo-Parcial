import { HttpErrorResponse } from '@angular/common/http';
import Swal, { SweetAlertIcon } from 'sweetalert2';
import { ErrorResponse } from 'src/app/models/respuesta';

/** Mensaje cuando no se logra contactar al backend (servidor apagado, sin red, CORS). */
const MENSAJE_SIN_CONEXION =
    'No hay conexión con el servidor de la clínica. Verifique su conexión e intente nuevamente.';

/** Mensaje genérico para cualquier error que no se pueda explicar al usuario. */
const MENSAJE_GENERICO = 'Ocurrió un error inesperado. Intente nuevamente.';

/**
 * Error ya traducido a lenguaje del usuario, listo para mostrarse.
 */
export interface ErrorPresentable {
    /** Texto comprensible, sin información técnica (URLs, clases, trazas). */
    mensaje: string;
    /** Ícono de la alerta según la gravedad. */
    icono: SweetAlertIcon;
}

/**
 * Traduce un error HTTP a un mensaje comprensible y un ícono según su gravedad.
 *
 * Reglas:
 * - Si el backend envió un mensaje de negocio (campo `message`), se usa tal cual.
 * - Nunca se muestra `error.message` de Angular (contiene la URL y el código técnico)
 *   ni cuerpos de texto crudos (pueden ser páginas HTML o trazas).
 * - 0 → sin conexión; 400 y 409 → advertencia; 404 → informativo; 5xx → error.
 *
 * @param error Error producido por la llamada HTTP (o cualquier otro).
 * @returns Mensaje e ícono para mostrar al usuario.
 */
export function clasificarError(error: unknown): ErrorPresentable {
    if (!(error instanceof HttpErrorResponse)) {
        return { mensaje: MENSAJE_GENERICO, icono: 'error' };
    }

    if (error.status === 0) {
        return { mensaje: MENSAJE_SIN_CONEXION, icono: 'error' };
    }

    const icono = iconoPorEstado(error.status);

    const cuerpo: unknown = error.error;
    if (cuerpo && typeof cuerpo === 'object') {
        const mensaje = (cuerpo as Partial<ErrorResponse>).message;
        if (typeof mensaje === 'string' && mensaje.trim().length > 0) {
            return { mensaje, icono };
        }
    }

    return { mensaje: mensajePorEstado(error.status), icono };
}

/**
 * Extrae solo el mensaje comprensible de un error.
 *
 * @param error Error producido por la llamada HTTP.
 * @returns Mensaje para el usuario.
 */
export function extraerMensajeError(error: unknown): string {
    return clasificarError(error).mensaje;
}

/**
 * Muestra una alerta de error con el ícono adecuado a la gravedad:
 * advertencia (400/409), informativo (404) o error (conexión/servidor).
 *
 * @param titulo Título de la alerta (qué se intentaba hacer).
 * @param error  Error producido por la llamada HTTP.
 */
export function mostrarError(titulo: string, error: unknown): void {
    const { mensaje, icono } = clasificarError(error);
    void Swal.fire({ title: titulo, text: mensaje, icon: icono });
}

/**
 * Ícono según el código HTTP.
 *
 * @param estado Código HTTP.
 */
function iconoPorEstado(estado: number): SweetAlertIcon {
    if (estado === 404) {
        return 'info';
    }
    if (estado === 400 || estado === 409 || estado === 422) {
        return 'warning';
    }
    return 'error';
}

/**
 * Mensaje de respaldo cuando el backend no envió uno.
 *
 * @param estado Código HTTP.
 */
function mensajePorEstado(estado: number): string {
    switch (estado) {
        case 400:
            return 'Los datos enviados no son válidos. Revise la información e intente nuevamente.';
        case 401:
        case 403:
            return 'No tiene permisos para realizar esta operación.';
        case 404:
            return 'El registro solicitado no fue encontrado.';
        case 409:
            return 'La operación no se puede realizar en el estado actual del registro.';
        default:
            return estado >= 500
                ? 'El servidor no pudo procesar la solicitud. Intente nuevamente en unos minutos.'
                : MENSAJE_GENERICO;
    }
}
