package com.ecommerce.devoluciones.garantias.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionHandlersTest {

    @Test
    void globalHandler_mapeaArgumentosInvalidosYConflictos() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MockHttpServletRequest request = crearRequest(
                "POST", "/api/v1/returns"
        );

        var badRequest = handler.manejarValidacion(
                new IllegalArgumentException("dato inválido"),
                request);

        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());

        Map<?, ?> badRequestBody = (Map<?, ?>) badRequest.getBody();

        assertEquals(400, badRequestBody.get("status"));
        assertEquals("INVALID_REQUEST", badRequestBody.get("code"));
        assertEquals("dato inválido", badRequestBody.get("message"));
        assertEquals("/api/v1/returns", badRequestBody.get("path"));
        assertNotNull(badRequestBody.get("timestamp"));

        request = crearRequest("GET", "/api/v1/returns/99");

        var missing = handler.manejarNoEncontrado(
                new java.util.NoSuchElementException("no existe"),
                request);

        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());

        Map<?, ?> missingBody = (Map<?, ?>) missing.getBody();

        assertEquals(404, missingBody.get("status"));
        assertEquals("RESOURCE_NOT_FOUND", missingBody.get("code"));
        assertEquals("no existe", missingBody.get("message"));
        assertEquals("/api/v1/returns/99", missingBody.get("path"));

        request = crearRequest("POST", "/api/v1/returns");

        var conflict = handler.manejarConflicto(
                new IllegalStateException("estado incompatible"),
                request);

        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());

        Map<?, ?> conflictBody = (Map<?, ?>) conflict.getBody();

        assertEquals(409, conflictBody.get("status"));
        assertEquals(
                "BUSINESS_RULE_VIOLATION",
                conflictBody.get("code")
        );
        assertEquals(
                "estado incompatible",
                conflictBody.get("message")
        );
    }

    @Test
    void globalHandler_usaMensajeDeValidacionOFallback() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MockHttpServletRequest request = crearRequest(
                "POST", "/api/v1/returns"
        );

        var response = handler.manejarErroresDeValidacion(
                validationException(List.of(
                        new FieldError(
                                "dto",
                                "orderId",
                                "El pedido es obligatorio"
                        )
                )),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();

        assertEquals(
                "El pedido es obligatorio",
                responseBody.get("message")
        );
        assertEquals("VALIDATION_ERROR", responseBody.get("code"));

        var fallback = handler.manejarErroresDeValidacion(
                validationException(List.of()),
                request
        );

        Map<?, ?> fallbackBody = (Map<?, ?>) fallback.getBody();

        assertEquals(
                "Los datos enviados no son válidos.",
                fallbackBody.get("message")
        );
    }

    @Test
    void globalHandler_mapeaViolacionesDeRestriccionConYSinMensaje() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MockHttpServletRequest request = crearRequest(
                "GET", "/api/v1/returns/0"
        );

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("debe ser positivo");

        var response = handler.manejarErrorParametroInvalido(
                new ConstraintViolationException(
                        "inválido",
                        Set.of(violation)
                ),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();

        assertEquals(400, responseBody.get("status"));
        assertEquals("VALIDATION_ERROR", responseBody.get("code"));
        assertEquals("debe ser positivo", responseBody.get("message"));

        var fallback = handler.manejarErrorParametroInvalido(
                new ConstraintViolationException(
                        "inválido",
                        Set.<ConstraintViolation<?>>of()
                ),
                request
        );

        Map<?, ?> fallbackBody = (Map<?, ?>) fallback.getBody();

        assertEquals(
                "El ID de la solicitud no es válido.",
                fallbackBody.get("message")
        );
    }

    @Test
    void returnDecisionHandler_construyeCuerpoComunDeError() {
        ReturnDecisionExceptionHandler handler = new ReturnDecisionExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/admin/returns/8/decision");

        var invalid = handler.manejarDecisionInvalida(new IllegalArgumentException("decisión inválida"), request);
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertEquals("INVALID_DECISION", invalid.getBody().get("code"));
        assertEquals("decisión inválida", invalid.getBody().get("message"));
        assertEquals("/api/v1/admin/returns/8/decision", invalid.getBody().get("path"));
        assertNotNull(invalid.getBody().get("timestamp"));
        assertEquals(400, invalid.getBody().get("status"));

        var notFound = handler.manejarNoEncontrado(new java.util.NoSuchElementException("no existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals("RETURN_NOT_FOUND", notFound.getBody().get("code"));

        var conflict = handler.manejarConflicto(new IllegalStateException("estado"), request);
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("INVALID_RETURN_STATUS", conflict.getBody().get("code"));
    }

    @Test
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
    }



    @Test
    void warrantyHandler_mapeaExcepcionesDeNegocio() {
        WarrantyExceptionHandler handler = new WarrantyExceptionHandler();
        MockHttpServletRequest request = crearRequest(
                "GET", "/api/warranties/claims/7"
        );

        var missing = handler.handleNotFound(
                new WarrantyNotFoundException("no existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("no existe", missing.getBody().message());
        assertEquals("/api/warranties/claims/7", missing.getBody().path());

        var rule = handler.handleRule(
                new WarrantyRuleException("garantía vencida"), request);
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, rule.getStatusCode());
        assertEquals("garantía vencida", rule.getBody().message());

        var badRequest = handler.handleBadRequest(
                new WarrantyBadRequestException("decisión incorrecta"), request);
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertEquals("decisión incorrecta", badRequest.getBody().message());
    }


    @Test
    void warrantyHandler_mapeaValidacionJsonInvalidoYErrorInesperado() {
        WarrantyExceptionHandler handler = new WarrantyExceptionHandler();
        MockHttpServletRequest request = crearRequest(
                "POST", "/api/warranties/claims"
        );

        var response = handler.handleValidation(validationException(List.of(
                new FieldError("dto", "userId", null),
                new FieldError("dto", "orderId", "orden inválida"))), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertTrue(response.getBody().message().contains("orderId: orden inválida"));
        assertTrue(response.getBody().message().contains("userId: valor inválido"));
        assertEquals("/api/warranties/claims", response.getBody().path());

        var unreadable = handler.handleUnreadable(
                mock(org.springframework.http.converter.HttpMessageNotReadableException.class),
                request);
        assertEquals(HttpStatus.BAD_REQUEST, unreadable.getStatusCode());
        assertEquals("MALFORMED_REQUEST", unreadable.getBody().code());
        assertEquals(
                "El cuerpo de la solicitud es inválido o contiene valores no permitidos.",
                unreadable.getBody().message());

        var unexpected = handler.handleUnexpected(
                new RuntimeException("detalle interno"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unexpected.getStatusCode());
        assertEquals("INTERNAL_ERROR", unexpected.getBody().code());
        assertEquals(
                "Ocurrió un error interno. Intenta de nuevo más tarde.",
                unexpected.getBody().message());
        assertEquals("/api/warranties/claims", unexpected.getBody().path());
    }



    private MethodArgumentNotValidException validationException(List<FieldError> errors) {
        BindingResult result = mock(BindingResult.class);
        when(result.getFieldErrors()).thenReturn(errors);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(result);
        return exception;
    }

    private MockHttpServletRequest crearRequest(
            String metodo,
            String ruta) {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setMethod(metodo);
        request.setRequestURI(ruta);

        return request;
    }
}
