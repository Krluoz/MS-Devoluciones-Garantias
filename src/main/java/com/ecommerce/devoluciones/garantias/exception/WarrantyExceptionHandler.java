package com.ecommerce.devoluciones.garantias.exception;

import com.ecommerce.devoluciones.garantias.controller.WarrantyClaimController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = WarrantyClaimController.class)
public class WarrantyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WarrantyExceptionHandler.class);

    @ExceptionHandler(WarrantyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            WarrantyNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(WarrantyRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleRule(
            WarrantyRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(WarrantyBadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            WarrantyBadRequestException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request);
    }

    // Falla de @Valid: un solo message con todos los campos inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": "
                        + (fe.getDefaultMessage() == null ? "valor inválido" : fe.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, WarrantyErrorCode.VALIDATION_ERROR, message, request);
    }

    // JSON mal formado o enum inexistente (por ejemplo reason = "ROTO")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, WarrantyErrorCode.MALFORMED_REQUEST,
                "El cuerpo de la solicitud es inválido o contiene valores no permitidos.", request);
    }

    // Por ejemplo GET /api/warranties/claims/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, WarrantyErrorCode.INVALID_PARAMETER,
                "El parámetro '" + ex.getName() + "' tiene un formato inválido.", request);
    }

    // Cualquier error inesperado: se registra completo en el log pero no se expone al cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, WarrantyErrorCode.INTERNAL_ERROR,
                "Ocurrió un error interno. Intenta de nuevo más tarde.", request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, WarrantyErrorCode code, String message, HttpServletRequest request) {
        ApiErrorResponse body =
                ApiErrorResponse.of(status, code.name(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}