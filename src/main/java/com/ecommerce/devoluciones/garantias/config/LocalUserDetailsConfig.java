package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.function.Function;

@Configuration
@Profile("local")
public class LocalUserDetailsConfig {

    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {
        return createUserDetailsService(passwordEncoder, System::getenv);
    }

    // Separar la lectura del entorno permite probar la configuración sin depender
    // de secretos establecidos en la máquina que ejecuta las pruebas.
    UserDetailsService createUserDetailsService(
            PasswordEncoder passwordEncoder,
            Function<String, String> environmentLookup) {

        UserDetails usuario101 = User.builder()
                .username("usuario101")
                .password(passwordEncoder.encode(
                        requiredPassword("MS05_USER101_PASSWORD", environmentLookup)))
                .roles("USER")
                .build();

        UserDetails usuario102 = User.builder()
                .username("usuario102")
                .password(passwordEncoder.encode(
                        requiredPassword("MS05_USER102_PASSWORD", environmentLookup)))
                .roles("USER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(
                        requiredPassword("MS05_ADMIN_PASSWORD", environmentLookup)))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(
                usuario101, usuario102, admin);

    }

    String requiredPassword(String variable, Function<String, String> environmentLookup) {
        String password = environmentLookup.apply(variable);

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable de entorno: " + variable);
        }

        return password;
    }*/
}