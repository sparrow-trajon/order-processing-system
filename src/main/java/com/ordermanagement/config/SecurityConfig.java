package com.ordermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for Order Management System.
 *
 * Current Mode: PERMISSIVE (All endpoints public)
 * This configuration is intentionally permissive to not break existing functionality.
 *
 * Future Enhancements:
 * - Add JWT authentication filter
 * - Implement role-based access control (RBAC)
 * - Add API key authentication for service-to-service calls
 * - Configure OAuth2 for third-party integrations
 *
 * Design Pattern: Chain of Responsibility (Security Filter Chain)
 * SOLID Principle: Single Responsibility - Security configuration only
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security in permissive mode.
     * All endpoints are currently public to maintain backward compatibility.
     *
     * Disabled Features (for now):
     * - CSRF (can be enabled for web forms in production)
     * - Authentication (JWT can be added later)
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for REST APIs (stateless, token-based auth)
                .csrf(AbstractHttpConfigurer::disable)

                // Permit all requests (no authentication required)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().permitAll()
                )

                // Disable frame options for H2 console
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        return http.build();
    }

    /**
     * Password encoder bean for future user authentication.
     * Uses BCrypt hashing algorithm (industry standard).
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * FUTURE IMPLEMENTATION: JWT Authentication
     *
     * Uncomment and implement when ready to add JWT auth:
     *
     * @Bean
     * public JwtAuthenticationFilter jwtAuthenticationFilter() {
     *     return new JwtAuthenticationFilter();
     * }
     *
     * @Bean
     * public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
     *     return config.getAuthenticationManager();
     * }
     *
     * Then update authorizeHttpRequests to:
     * .requestMatchers("/api/auth/**").permitAll()
     * .requestMatchers("/api/**").authenticated()
     */
}
