package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

//temp: usuarios de prueba mientras no se integre la autenticación real.
//Se tiene que borrar esta clase cuando se use el token del microservicio de usuarios.
@Configuration
public class DevUsersConfig {

    @Bean
    public InMemoryUserDetailsManager devUsers() {
        return new InMemoryUserDetailsManager(
                User.withUsername("1").password("{noop}cliente1").roles("USER").build(),
                User.withUsername("2").password("{noop}cliente2").roles("USER").build(),
                User.withUsername("99").password("{noop}admin99").roles("ADMIN").build()
        );
    }
}