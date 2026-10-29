package com.ecommerce.devoluciones.garantias.security;

import org.springframework.security.core.Authentication;

public record AuthenticatedUser(Long id, boolean admin) {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    //username es el id, con JWT aqui leen sus claims.
    public static AuthenticatedUser from(Authentication authentication) {
        Long id = Long.valueOf(authentication.getName());
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> ADMIN_AUTHORITY.equals(a.getAuthority()));
        return new AuthenticatedUser(id, admin);
    }
}