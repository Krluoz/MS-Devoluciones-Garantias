package com.ecommerce.devoluciones.garantias.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.junit.jupiter.api.Assertions.*;

class DevUsersConfigTest {

    @Test
    void configuraUsuariosDeDesarrolloConRolesYCredencialesActuales() {
        DevUsersConfig config = new DevUsersConfig();
        InMemoryUserDetailsManager users = config.devUsers();

        UserDetails cliente1 = users.loadUserByUsername("1");
        UserDetails cliente2 = users.loadUserByUsername("2");
        UserDetails admin = users.loadUserByUsername("99");

        assertTrue(cliente1.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(cliente2.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(admin.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        assertEquals("{noop}cliente1", cliente1.getPassword());
        assertEquals("{noop}cliente2", cliente2.getPassword());
        assertEquals("{noop}admin99", admin.getPassword());
    }

    @Test
    void usuariosDeDesarrolloNoIncluyenNombresObsoletos() {
        InMemoryUserDetailsManager users = new DevUsersConfig().devUsers();

        assertFalse(users.userExists("usuario101"));
        assertFalse(users.userExists("usuario102"));
        assertFalse(users.userExists("admin"));
    }
}
