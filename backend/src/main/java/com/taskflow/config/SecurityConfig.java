package com.taskflow.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Main Spring Security configuration class.
 * Configures HTTP security, CORS, session management, and password encoding.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables method-level security like @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    // We will inject the UserDetailsService implementation from the next file
    private final UserDetailsService userDetailsService;

    /**
     * Defines the PasswordEncoder bean (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * Required for the /login endpoint to manually authenticate users.
     */
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the main security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        http
            // 1. Disable CSRF (Cross-Site Request Forgery)
            // We are building a stateless REST API with JWTs, so CSRF protection is not needed.
            .csrf(AbstractHttpConfigurer::disable)
            // 2. Configure CORS (Cross-Origin Resource Sharing)
            // We'll allow our frontend (running on a different port/domain) to call the API.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 3. Set Session Management to STATELESS
            // This tells Spring Security not to create or use sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 4. Define authorization rules for endpoints
            .authorizeHttpRequests(authorize ->
                authorize
                    // Publicly accessible endpoints
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    // OpenAPI/Swagger UI endpoints
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api/v1/api-docs/**"
                    )
                    .permitAll()
                    // All other requests must be authenticated
                    .anyRequest()
                    .authenticated()
            )
            // 5. Tell Spring Security how to load user details
            .userDetailsService(userDetailsService);

        // In Step 7, we will add our JwtAuthenticationFilter here, before the default username/password filter.

        return http.build();
    }

    /**
     * Configures CORS policy
     * This basic config allows a local React app (http://localhost:3000) to connect.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow our local frontend
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Allow all standard methods
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        // Allow credentials (e.g., cookies, auth headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this config to all paths
        return source;
    }
}
