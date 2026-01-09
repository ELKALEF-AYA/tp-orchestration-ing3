package com.example.product.infrastructure.config;


import com.example.product.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Pas de CSRF pour une API REST
                .csrf(csrf -> csrf.disable())

                // Pas de session (JWT only)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Règles d’accès
                .authorizeHttpRequests(auth -> auth
                        // Swagger & Actuator
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/h2-console/**"
                        ).permitAll()

                        // API PRODUCT protégée
                        .requestMatchers("/api/v1/products/**")
                        .hasRole("USER")

                        // Tout le reste interdit
                        .anyRequest().denyAll()
                )

                // Ajout du filtre JWT
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Désactive le login par défaut
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // Pour H2 Console
        http.headers(headers ->
                headers.frameOptions(frame -> frame.disable())
        );

        return http.build();
    }
}
