package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;


@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Solo los administradores pueden  aprobar o rechazar devoluciones
                        .requestMatchers("/api/admin/returns/**").hasRole("ADMIN")
                        // Los usuarios autenticados pueden consultar sus propias devoluciones
                        .requestMatchers(HttpMethod.GET, "/api/v1/returns/me").hasAnyRole("USER")
                        .requestMatchers("/api/v1/returns/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
