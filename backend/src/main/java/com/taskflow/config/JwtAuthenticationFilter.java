package com.taskflow.config;

import com.taskflow.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that runs once per request to validate the JWT.
 */
@Component
@RequiredArgsConstructor
@Slf4j // Simple Logging Facade for Java
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check for Authorization header and "Bearer " prefix
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to next filter
            return;
        }

        // 2. Extract the token
        jwt = authHeader.substring(7); // "Bearer ".length()

        try {
            // 3. Extract email (username) from token
            userEmail = jwtService.extractUsername(jwt);

            // 4. Check if user is already authenticated
            if (
                userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                // 5. Load user details from the database
                UserDetails userDetails =
                    this.userDetailsService.loadUserByUsername(userEmail);

                // 6. Validate the token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // 7. Create an authentication token
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // We don't need credentials for JWT-based auth
                            userDetails.getAuthorities()
                        );

                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(
                            request
                        )
                    );

                    // 8. Set the authentication in the SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(
                        authToken
                    );
                    log.debug("User {} authenticated successfully", userEmail);
                } else {
                    log.warn("Invalid JWT token for user {}", userEmail);
                }
            }
        } catch (Exception e) {
            // Handle exceptions like expired token, invalid signature, etc.
            log.warn("Cannot set user authentication: {}", e.getMessage());
        }

        // 9. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
