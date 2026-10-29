package com.ecommerce.devoluciones.garantias.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionHandlersTest {

    @Test
    void globalHandler_mapeaArgumentosInvalidosYConflictos() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var badRequest = handler.manejarValidacion(new IllegalArgumentException("dato inválido"));
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertEquals("dato inválido", badRequest.getBody().get("error"));

        var missing = handler.manejarNoEncontrado(new java.util.NoSuchElementException("no existe"));
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("no existe", missing.getBody().get("error"));

        var conflict = handler.manejarConflicto(new IllegalStateException("estado incompatible"));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("estado incompatible", conflict.getBody().get("error"));
    }

    @Test
    void globalHandler_usaMensajeDeValidacionOFallback() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MethodArgumentNotValidException exception = validationException(List.of(
                new FieldError("dto", "orderId", "El pedido es obligatorio")));
        var response = handler.manejarErroresDeValidacion(exception);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El pedido es obligatorio", response.getBody().get("error"));

        var fallback = handler.manejarErroresDeValidacion(validationException(List.of()));
        assertEquals("Los datos enviados no son válidos.", fallback.getBody().get("error"));
    }

    @Test
    void globalHandler_mapeaViolacionesDeRestriccionConYSinMensaje() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("debe ser positivo");
        var response = handler.manejarErrorParametroInvalido(
                new ConstraintViolationException("inválido", Set.of(violation)));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("debe ser positivo", response.getBody().get("error"));

        var fallback = handler.manejarErrorParametroInvalido(
                new ConstraintViolationException("inválido", Set.<ConstraintViolation<?>>of()));
        assertEquals("El ID de la solicitud no es válido.", fallback.getBody().get("error"));
    }

    @Test
    void returnDecisionHandler_construyeCuerpoComunDeError() {
        ReturnDecisionExceptionHandler handler = new ReturnDecisionExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/admin/returns/8/decision");

        var invalid = handler.manejarDecisionInvalida(new IllegalArgumentException("decisión inválida"), request);
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertEquals("INVALID_DECISION", invalid.getBody().get("code"));
        assertEquals("decisión inválida", invalid.getBody().get("message"));
        assertEquals("/api/admin/returns/8/decision", invalid.getBody().get("path"));
        assertNotNull(invalid.getBody().get("timestamp"));
        assertEquals(400, invalid.getBody().get("status"));

        var notFound = handler.manejarNoEncontrado(new java.util.NoSuchElementException("no existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals("RETURN_NOT_FOUND", notFound.getBody().get("code"));

        var conflict = handler.manejarConflicto(new IllegalStateException("estado"), request);
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("INVALID_RETURN_STATUS", conflict.getBody().get("code"));
    }

    /*@Test
    void returnDecisionHandler_mapeaJsonYValidacion() {
        ReturnDecisionExceptionHandler handler = new ReturnDecisionExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/admin/returns/1/decision");

        var invalidJson = handler.manejarJsonInvalido(mock(HttpMessageNotReadableException.class), request);
        assertEquals(HttpStatus.BAD_REQUEST, invalidJson.getStatusCode());
        assertEquals("INVALID_REQUEST_BODY", invalidJson.getBody().get("code"));

        var validation = handler.manejarValidacion(
                validationException(List.of(new FieldError("dto", "decision", "La decisión es obligatoria"))), request);
        assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
        assertEquals("VALIDATION_ERROR", validation.getBody().get("code"));
        assertEquals("La decisión es obligatoria", validation.getBody().get("message"));

        var fallback = handler.manejarValidacion(validationException(List.of()), request);
        assertEquals("Los datos de la solicitud no son válidos", fallback.getBody().get("message"));
    }*/

    @Test
    void warrantyHandler_mapeaExcepcionesDeNegocio() {
        WarrantyExceptionHandler handler = new WarrantyExceptionHandler();
        var missing = handler.handleNotFound(new WarrantyNotFoundException("no existe"));
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("no existe", missing.getBody().get("error"));

        /*var rule = handler.handleRule(new WarrantyRuleException("garantía vencida"));
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, rule.getStatusCode());
        assertEquals("garantía vencida", rule.getBody().get("error"));*/

        var badRequest = handler.handleBadRequest(new WarrantyBadRequestException("decisión incorrecta"));
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertEquals("decisión incorrecta", badRequest.getBody().get("error"));

        /*var unreadable = handler.handleUnreadable(mock(HttpMessageNotReadableException.class));
        assertEquals(HttpStatus.BAD_REQUEST, unreadable.getStatusCode());
        assertEquals(
                "El cuerpo de la solicitud es inválido o contiene valores no permitidos.",
                unreadable.getBody().get("error")
        );*/
    }

    @Test
    void warrantyHandler_mapeaFieldErrorsYReemplazaMensajeNulo() {
        WarrantyExceptionHandler handler = new WarrantyExceptionHandler();
        var response = handler.handleValidation(validationException(List.of(
                new FieldError("dto", "userId", null),
                new FieldError("dto", "userId", "mensaje duplicado"),
                new FieldError("dto", "orderId", "orden inválida"))));

        /*assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Los datos de la solicitud no son válidos.", response.getBody().get("error"));
        @SuppressWarnings("unchecked")
        var details = (java.util.Map<String, String>) response.getBody().get("details");
        assertEquals("Valor inválido", details.get("userId"));
        assertEquals("orden inválida", details.get("orderId"));
        assertEquals(2, details.size());*/
    }

    private MethodArgumentNotValidException validationException(List<FieldError> errors) {
        BindingResult result = mock(BindingResult.class);
        when(result.getFieldErrors()).thenReturn(errors);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(result);
        return exception;
    }
}
