package com.ecommerce.devoluciones.garantias.config;

import com.ecommerce.devoluciones.garantias.exception.WarrantyErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiSecurityErrorHandlerTest {

    private ApiSecurityErrorHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new ApiSecurityErrorHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/warranties/claims/99");
        response = new MockHttpServletResponse();
    }

    @Test
    void commence_devuelve401YFormatoJson() throws IOException {
        AuthenticationException exception =
                mock(AuthenticationException.class);

        handler.commence(request, response, exception);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals("Basic realm=\"warranties\"",
                response.getHeader("WWW-Authenticate"));

        String json = response.getContentAsString();

        assertTrue(json.contains("\"status\":401"));
        assertTrue(json.contains("\"code\":\""
                + WarrantyErrorCode.UNAUTHORIZED.name() + "\""));
        assertTrue(json.contains("\"message\":\"Se requiere autenticación válida.\""));
        assertTrue(json.contains("\"path\":\"/api/v1/warranties/claims/99\""));
    }

    @Test
    void handle_devuelve403YFormatoJson() throws IOException {
        AccessDeniedException exception =
                new AccessDeniedException("Acceso denegado");

        handler.handle(request, response, exception);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        String json = response.getContentAsString();

        assertTrue(json.contains("\"status\":403"));
        assertTrue(json.contains("\"code\":\""
                + WarrantyErrorCode.FORBIDDEN.name() + "\""));
        assertTrue(json.contains(
                "\"message\":\"No tienes permisos para realizar esta acción.\""));
        assertTrue(json.contains("\"path\":\"/api/v1/warranties/claims/99\""));
    }

    @Test
    void commence_escapaComillasYBarrasInvertidasEnLaRuta() throws IOException {
        request.setRequestURI("/api/test/\"recurso\\id");

        handler.commence(
                request,
                response,
                mock(AuthenticationException.class));

        String json = response.getContentAsString();

        assertTrue(json.contains("\"path\":\"/api/test/\\\"recurso\\\\id\""));
    }

    @Test
    void write_configuraRespuestaHttpCorrectamente() throws IOException {
        handler.handle(
                request,
                response,
                new AccessDeniedException("sin permisos"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertNotNull(response.getContentAsString());
        assertFalse(response.getContentAsString().isBlank());
    }
}

