
package com.ecommerce.devoluciones.garantias.exception;

import com.ecommerce.devoluciones.garantias.controller.ReturnAdminController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ReturnAdminController.class)
public class ReturnDecisionExceptionHandler {



    // Cuando la decisión viene null o está ausente
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Los datos de la solicitud no son válidos");

        return construirError(HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                mensaje,
                request
        );
    }

    // Cuando el JSON es inválido o contiene un enum desconocido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return construirError(HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "El JSON contiene datos inválidos o una decisión no permitida",
                request
        );
    }


    // Decisión no permitida - HTTP 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarDecisionInvalida(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return construirError(
                HttpStatus.BAD_REQUEST,
                "INVALID_DECISION",
                ex.getMessage(),
                request
        );
    }

    // Devolución inexistente - HTTP 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            NoSuchElementException ex,
            HttpServletRequest request) {

        return construirError(
                HttpStatus.NOT_FOUND,
                "RETURN_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    // Estado incompatible - HTTP 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarConflicto(
            IllegalStateException ex,
            HttpServletRequest request) {

        return construirError(
                HttpStatus.CONFLICT,
                "INVALID_RETURN_STATUS",
                ex.getMessage(),
                request
        );
    }



    private ResponseEntity<Map<String, Object>> construirError(
            HttpStatus status,
            String codigo,
            String mensaje,
            HttpServletRequest request) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("timestamp", Instant.now().toString());
        respuesta.put("status", status.value());
        respuesta.put("code", codigo);
        respuesta.put("message", mensaje);
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .status(status)
                .body(respuesta);
    }

}
