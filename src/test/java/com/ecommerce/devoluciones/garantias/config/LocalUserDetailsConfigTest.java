package com.ecommerce.devoluciones.garantias.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LocalUserDetailsConfigTest {

    @Test
    void creaPasswordEncoderDelegado() {
        LocalUserDetailsConfig config = new LocalUserDetailsConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("contraseña-de-prueba");

        assertNotEquals("contraseña-de-prueba", encoded);
        assertTrue(encoder.matches("contraseña-de-prueba", encoded));
    }

    @Test
    void construyeUsuariosLocalesConRolesYContrasenasCodificadas() {
        LocalUserDetailsConfig config = new LocalUserDetailsConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        Map<String, String> environment = Map.of(
                "MS05_USER101_PASSWORD", "secreto-101",
                "MS05_USER102_PASSWORD", "secreto-102",
                "MS05_ADMIN_PASSWORD", "secreto-admin");

        UserDetailsService service = config.createUserDetailsService(encoder, environment::get);

        var user101 = service.loadUserByUsername("usuario101");
        var user102 = service.loadUserByUsername("usuario102");
        var admin = service.loadUserByUsername("admin");
        assertTrue(user101.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(user102.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(admin.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(encoder.matches("secreto-101", user101.getPassword()));
        assertTrue(encoder.matches("secreto-102", user102.getPassword()));
        assertTrue(encoder.matches("secreto-admin", admin.getPassword()));
    }

    @Test
    void rechazaVariableDeContrasenaNulaOVacia() {
        LocalUserDetailsConfig config = new LocalUserDetailsConfig();
        assertThrows(IllegalStateException.class,
                () -> config.requiredPassword("NO_CONFIGURADA", variable -> null));
        assertThrows(IllegalStateException.class,
                () -> config.requiredPassword("EN_BLANCO", variable -> "   "));
        assertEquals("valor", config.requiredPassword("CONFIGURADA", variable -> "valor"));
    }


    @Test
    void elMetodoBeanLeeLasVariablesDeEntornoOSenalaCualFalta() {
        LocalUserDetailsConfig config = new LocalUserDetailsConfig();
        try {
            UserDetailsService service = config.userDetailsService(config.passwordEncoder());
            assertNotNull(service.loadUserByUsername("admin"));
        } catch (IllegalStateException exception) {
            assertTrue(exception.getMessage().contains("MS05_"));
        }
    }
}
