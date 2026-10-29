package com.ecommerce.devoluciones.garantias.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserIdResolverTest {

    private Authentication authentication(String name, boolean authenticated) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(name);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        return authentication;
    }

    @Test
    void authenticatedResolver_resuelveIdsConfigurados() {
        AuthenticatedUserIdResolver resolver = new AuthenticatedUserIdResolver();
        assertEquals(101L, resolver.obtenerUserId(authentication("101", true)));
        assertEquals(102L, resolver.obtenerUserId(authentication("102", true)));
    }

    @Test
    void authenticatedResolver_rechazaAutenticacionesNoValidas() {
        AuthenticatedUserIdResolver resolver = new AuthenticatedUserIdResolver();
        assertThrows(IllegalArgumentException.class, () -> resolver.obtenerUserId(null));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.obtenerUserId(authentication("usuario101", false)));

        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        assertThrows(IllegalArgumentException.class, () -> resolver.obtenerUserId(anonymous));
    }

    @Test
    void authenticatedResolver_rechazaUsuarioSinIdNumerico() {
        AuthenticatedUserIdResolver resolver = new AuthenticatedUserIdResolver();
        assertThrows(
                NumberFormatException.class,
                () -> resolver.obtenerUserId(authentication("sin-mapeo", true))
        );
    }


    @Test
    void testResolver_exigeAutenticacionYUsaUsuario101() {
        TestUserIdResolver resolver = new TestUserIdResolver();
        assertThrows(IllegalArgumentException.class, () -> resolver.obtenerUserId(null));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.obtenerUserId(authentication("usuario101", false)));
        assertEquals(101L, resolver.obtenerUserId(authentication("cualquier-usuario", true)));
    }
}
