package com.ecommerce.devoluciones.garantias.exception;

import com.ecommerce.devoluciones.garantias.controller.WarrantyClaimController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Limitado al controller de garantías para no interferir con el GlobalExceptionHandler
// de devoluciones. Al unificar, se puede quitar assignableTypes.
@RestControllerAdvice(assignableTypes = WarrantyClaimController.class)
public class WarrantyExceptionHandler {

    @ExceptionHandler(WarrantyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(WarrantyNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WarrantyRuleException.class)
    public ResponseEntity<Map<String, Object>> handleRule(WarrantyRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(WarrantyBadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(WarrantyBadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Falla de @Valid: devuelve qué campo falló y por qué
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.putIfAbsent(fe.getField(),
                    fe.getDefaultMessage() == null ? "Valor inválido" : fe.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Los datos de la solicitud no son válidos.");
        body.put("details", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // JSON mal formado o enum inexistente (por ejemplo reason = "ROTO")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido o contiene valores no permitidos.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}