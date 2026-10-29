package com.ecommerce.devoluciones.garantias.config;

import com.ecommerce.devoluciones.garantias.exception.ApiErrorResponse;
import com.ecommerce.devoluciones.garantias.exception.WarrantyErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Responde 401 y 403 con el mismo formato común de error del resto de la API
@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // 401: no hay credenciales o son inválidas
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"warranties\"");
        write(request, response, HttpStatus.UNAUTHORIZED, WarrantyErrorCode.UNAUTHORIZED,
                "Se requiere autenticación válida.");
    }

    // 403: autenticado, pero sin permisos
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, WarrantyErrorCode.FORBIDDEN,
                "No tienes permisos para realizar esta acción.");
    }

    private void write(HttpServletRequest request, HttpServletResponse response,
                       HttpStatus status, WarrantyErrorCode code, String message) throws IOException {
        ApiErrorResponse body =
                ApiErrorResponse.of(status, code.name(), message, request.getRequestURI());

        String json = "{"
                + "\"timestamp\":\"" + body.timestamp() + "\","
                + "\"status\":" + body.status() + ","
                + "\"code\":\"" + body.code() + "\","
                + "\"message\":\"" + body.message() + "\","
                + "\"path\":\"" + escape(body.path()) + "\""
                + "}";

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}