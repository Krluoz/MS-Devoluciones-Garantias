package com.ecommerce.devoluciones.garantias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, ApiSecurityErrorHandler errorHandler) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Admin (rutas actuales y futuras)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/returns/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/warranties/claims/**").hasRole("ADMIN")
                        // Devoluciones (/me antes que {id})
                        .requestMatchers(HttpMethod.POST, "/api/v1/returns").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/returns/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/returns/{id}").hasRole("ADMIN")
                        // Garantías
                        .requestMatchers(HttpMethod.POST, "/api/v1/warranties/claims").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/warranties/claims/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/warranties/claims/{id}").hasAnyRole("USER", "ADMIN")
                        // Rutas internas de Spring para mostrar errores
                        .requestMatchers("/error").permitAll()
                        // Todo lo demás, cerrado
                        .anyRequest().denyAll()
                )
                .httpBasic(b -> b.authenticationEntryPoint(errorHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));

        return http.build();
    }
}