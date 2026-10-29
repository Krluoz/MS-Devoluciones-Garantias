package com.ecommerce.devoluciones.garantias.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {

    public static ApiErrorResponse of(HttpStatus status, String code, String message, String path) {
        return new ApiErrorResponse(
                Instant.now().truncatedTo(ChronoUnit.SECONDS),
                status.value(),
                code,
                message,
                path
        );
    }
}