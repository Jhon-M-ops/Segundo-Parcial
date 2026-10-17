package com.uniminuto.clinica.exception;

/**
 * Se lanza cuando un recurso solicitado no existe. Se traduce a HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    /**
     * @param message mensaje comprensible para el usuario.
     */
    public NotFoundException(String message) {
        super(message);
    }
}
