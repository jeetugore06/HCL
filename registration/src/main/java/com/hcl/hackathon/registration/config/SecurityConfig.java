package com.hcl.hackathon.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Open all endpoints — authentication and authorization are enforced by the
 * upstream API gateway. The registration service trusts requests that reach it.
 *
 * CSRF is disabled because the service is stateless REST and does not use
 * cookie-based sessions. HTTP Basic and form login are explicitly disabled so
 * Spring Security's auto-config doesn't surface a generated password or login
 * page.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .build();
    }
}
