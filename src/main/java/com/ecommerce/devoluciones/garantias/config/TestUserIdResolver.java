package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestUserIdResolver implements UserIdResolver {

    @Override
    public Long obtenerUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("El usuario no está autenticado");
        }

        return 101L;
    }
}
