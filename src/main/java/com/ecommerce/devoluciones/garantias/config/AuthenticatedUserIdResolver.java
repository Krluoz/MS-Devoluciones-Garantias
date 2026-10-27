
package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("local")
public class AuthenticatedUserIdResolver implements UserIdResolver {

    private static final Map<String, Long> USUARIOS = Map.of(
            "usuario101", 101L,
            "usuario102", 102L
    );

    @Override
    public Long obtenerUserId(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalArgumentException(
                    "El usuario no está autenticado");
        }

        Long userId = USUARIOS.get(authentication.getName());

        if (userId == null) {
            throw new IllegalStateException(
                    "El usuario autenticado no tiene un ID asociado");
        }

        return userId;
    }
}
