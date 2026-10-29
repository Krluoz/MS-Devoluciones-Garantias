package com.ecommerce.devoluciones.garantias.config;

import com.ecommerce.devoluciones.garantias.security.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AuthenticatedUserIdResolver implements UserIdResolver {

    @Override
    public Long obtenerUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("El usuario no está autenticado");
        }
        return AuthenticatedUser.from(authentication).id();
    }
}
