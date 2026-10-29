package com.ecommerce.devoluciones.garantias.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private boolean esEndpointDeDevolucionPropio(
            HttpServletRequest request) {

        String metodo = request.getMethod();
        String ruta = request.getRequestURI()
                .substring(request.getContextPath().length());

        // POST /api/returns
        if ("POST".equals(metodo)
                && "/api/v1/returns".equals(ruta)) {
            return true;
        }

        // GET /api/returns/{id}
        return "GET".equals(metodo)
                && ruta.matches("/api/v1/returns/\\d+");
    }

    private ResponseEntity<Map<String, Object>> construirRespuestaError(
            HttpStatus status,
            String codigo,
            String mensaje,
            HttpServletRequest request) {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("timestamp", Instant.now().toString());
        error.put("status", status.value());
        error.put("code", codigo);
        error.put("message", mensaje);
        error.put("path", request.getRequestURI()
                .substring(request.getContextPath().length()));

        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<?> responderError(
            HttpStatus status,
            String codigo,
            String mensaje,
            HttpServletRequest request) {

        if (esEndpointDeDevolucionPropio(request)) {
            return construirRespuestaError(
                    status,
                    codigo,
                    mensaje,
                    request
            );
        }

        // Mantener el formato actual para los demás endpoints.
        return ResponseEntity
                .status(status)
                .body(Map.of("error", mensaje));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> manejarValidacion(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return responderError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> manejarErroresDeValidacion(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Los datos enviados no son válidos.");

        return responderError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                mensaje,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> manejarErrorParametroInvalido(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String mensaje = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("El ID de la solicitud no es válido.");

        return responderError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                mensaje,
                request
        );
    }

    @ExceptionHandler(DuplicateReturnRequestException.class)
    public ResponseEntity<?> manejarSolicitudDuplicada(
            DuplicateReturnRequestException ex,
            HttpServletRequest request) {

        return responderError(
                HttpStatus.CONFLICT,
                "DUPLICATE_RETURN_REQUEST",
                ex.getMessage(),
                request
        );
    }

    // Devolución no encontrada - HTTP 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> manejarNoEncontrado(
            NoSuchElementException ex,
            HttpServletRequest request) {

        return responderError(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    // Conflicto con el estado actual - HTTP 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> manejarConflicto(
            IllegalStateException ex,
            HttpServletRequest request) {

        return responderError(
                HttpStatus.CONFLICT,
                "BUSINESS_RULE_VIOLATION",
                ex.getMessage(),
                request
        );
    }

}

