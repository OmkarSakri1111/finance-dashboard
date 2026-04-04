package com.financeapp.financeservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayAuthFilter gatewayAuthFilter;

    public SecurityConfig(GatewayAuthFilter gatewayAuthFilter) {
        this.gatewayAuthFilter = gatewayAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/finance/health").permitAll()
                // Dashboard summary — ANALYST and ADMIN
                .requestMatchers(HttpMethod.GET, "/finance/dashboard/**").hasAnyRole("ANALYST", "ADMIN")
                // Read records — all authenticated roles
                .requestMatchers(HttpMethod.GET, "/finance/records/**").hasAnyRole("VIEWER", "ANALYST", "ADMIN")
                // Write records — ANALYST and ADMIN
                .requestMatchers(HttpMethod.POST, "/finance/records/**").hasAnyRole("ANALYST", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/finance/records/**").hasAnyRole("ANALYST", "ADMIN")
                // Delete — ADMIN only
                .requestMatchers(HttpMethod.DELETE, "/finance/records/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
