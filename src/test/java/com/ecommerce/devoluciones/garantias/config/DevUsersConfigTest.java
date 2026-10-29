package com.ecommerce.devoluciones.garantias.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.junit.jupiter.api.Assertions.*;

class DevUsersConfigTest {

    @Test
    void construyeUsuariosLocalesConRolesYContrasenas() {
        DevUsersConfig config = new DevUsersConfig();

        InMemoryUserDetailsManager service = config.devUsers();

        UserDetails user1 = service.loadUserByUsername("1");
        UserDetails user2 = service.loadUserByUsername("2");
        UserDetails admin = service.loadUserByUsername("99");

        assertTrue(user1.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertTrue(user2.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        assertTrue(admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void usuariosTienenLasContrasenasConfiguradas() {
        DevUsersConfig config = new DevUsersConfig();

        InMemoryUserDetailsManager service = config.devUsers();

        assertEquals("{noop}cliente1",
                service.loadUserByUsername("1").getPassword());

        assertEquals("{noop}cliente2",
                service.loadUserByUsername("2").getPassword());

        assertEquals("{noop}admin99",
                service.loadUserByUsername("99").getPassword());
    }

    @Test
    void usuarioInexistenteEsRechazado() {
        DevUsersConfig config = new DevUsersConfig();

        InMemoryUserDetailsManager service = config.devUsers();

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("usuario-inexistente"));
    }
}


