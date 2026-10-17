package com.uniminuto.clinica.exception;

/**
 * Se lanza cuando la operación entra en conflicto con el estado actual del
 * recurso (por ejemplo, editar una fórmula inactiva). Se traduce a HTTP 409.
 */
public class ConflictException extends RuntimeException {

    /**
     * @param message mensaje comprensible para el usuario.
     */
    public ConflictException(String message) {
        super(message);
    }
}
