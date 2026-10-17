package com.uniminuto.clinica.exception;

import com.uniminuto.clinica.exception.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja la excepción de negocio propia del proyecto.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                "BAD REQUEST",
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(error);
    }

    /**
     * Maneja la excepción de validación usada por la capa de servicios.
     * Sin este handler las validaciones de negocio terminaban como HTTP 500
     * porque solo existia un handler genérico para Exception.
     */
    @ExceptionHandler(org.apache.coyote.BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleCoyoteBadRequest(
            org.apache.coyote.BadRequestException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja los recursos no encontrados (HTTP 404).
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "NOT FOUND",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Maneja los conflictos con el estado actual del recurso (HTTP 409).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Maneja los parámetros obligatorios faltantes en la URL.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "El parámetro obligatorio '" + ex.getParameterName() + "' no fue enviado"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja los cuerpos JSON mal formados o con tipos incompatibles.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "El cuerpo de la solicitud no es válido"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja las violaciones de integridad de la base de datos (llaves foráneas,
     * valores duplicados, campos obligatorios). Responde 409 con un mensaje que
     * el usuario entienda, sin exponer detalles técnicos.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        LOGGER.warn("Violación de integridad de datos", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                "No se pudo completar la operación: el registro está relacionado con otra "
                        + "información (citas, historias o fórmulas) o ya existe"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Maneja los datos con formato inválido en la URL o en los parámetros
     * (por ejemplo un id que no es numérico o una fecha mal escrita).
     */
    @ExceptionHandler({TypeMismatchException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleFormatoInvalido(Exception ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD REQUEST",
                "Uno de los datos enviados tiene un formato no válido"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja cualquier otra excepción no contemplada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        // Las excepciones de Spring MVC (405, 415, ruta inexistente...) traen su propio
        // código HTTP: se respeta en lugar de convertirlo en un 500.
        if (ex instanceof org.springframework.web.ErrorResponse errorSpring) {
            HttpStatusCode estado = errorSpring.getStatusCode();
            if (estado.is4xxClientError()) {
                ErrorResponse error = new ErrorResponse(
                        estado.value(), etiquetaPorEstado(estado), mensajePorEstado(estado));
                return ResponseEntity.status(estado).body(error);
            }
        }
        // Se registra el detalle técnico en el log, pero NO se envía al cliente.
        LOGGER.error("Error inesperado", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL SERVER_ERROR",
                "Ocurrió un error inesperado. Intente nuevamente."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    /**
     * Mensaje comprensible para un código 4xx generado por Spring MVC.
     *
     * @param estado código HTTP.
     * @return mensaje para el usuario.
     */
    private String mensajePorEstado(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 404 -> "El recurso solicitado no existe";
            case 405 -> "El método HTTP no está permitido para este recurso";
            case 415 -> "El formato del contenido enviado no es soportado";
            default -> "La solicitud no es válida";
        };
    }

    /**
     * Etiqueta corta del tipo de error, coherente con el resto de respuestas.
     *
     * @param estado código HTTP.
     * @return etiqueta del error.
     */
    private String etiquetaPorEstado(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 404 -> "NOT FOUND";
            case 405 -> "METHOD NOT ALLOWED";
            case 415 -> "UNSUPPORTED MEDIA TYPE";
            default -> "BAD REQUEST";
        };
    }
}