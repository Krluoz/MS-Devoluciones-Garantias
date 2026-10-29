
package com.ecommerce.devoluciones.garantias.config;

import com.ecommerce.devoluciones.garantias.exception.WarrantyErrorCode;
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
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSecurityErrorHandlerTest {

    private final ApiSecurityErrorHandler handler =
            new ApiSecurityErrorHandler();

    @Test
    void commence_debeResponder401ConFormatoComunDeError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/garantias");

        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(
                request,
                response,
                new BadCredentialsException("Credenciales inválidas")
        );

        String body = response.getContentAsString();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(
                "Basic realm=\"warranties\"",
                response.getHeader("WWW-Authenticate")
        );
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains(
                "\"code\":\"" + WarrantyErrorCode.UNAUTHORIZED.name() + "\""
        ));
        assertTrue(body.contains(
                "\"message\":\"Se requiere autenticación válida.\""
        ));
        assertTrue(body.contains("\"path\":\"/api/garantias\""));
    }

    @Test
    void handle_debeResponder403ConFormatoComunDeError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/garantias/123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                request,
                response,
                new AccessDeniedException("Acceso denegado")
        );

        String body = response.getContentAsString();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains(
                "\"code\":\"" + WarrantyErrorCode.FORBIDDEN.name() + "\""
        ));
        assertTrue(body.contains(
                "\"message\":\"No tienes permisos para realizar esta acción.\""
        ));
        assertTrue(body.contains("\"path\":\"/api/garantias/123\""));
    }

    @Test
    void commence_debeEscaparComillasYBarrasInvertidasEnLaRuta()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/garantias/\"especial\\ruta");

        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(
                request,
                response,
                new BadCredentialsException("Credenciales inválidas")
        );

        String body = response.getContentAsString();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        // La comilla y la barra invertida deben aparecer escapadas en el JSON.
        assertTrue(body.contains(
                "\"path\":\"/api/garantias/\\\"especial\\\\ruta\""
        ));
    }
}
