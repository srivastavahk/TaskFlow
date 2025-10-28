package com.taskflow.controller;

import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.LoginResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     * Endpoint for new user registration.
     *
     * @param registerRequest The request body containing user details.
     * @return A ResponseEntity with the created user DTO and HTTP status 201.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
        @Valid @RequestBody RegisterRequest registerRequest
    ) {
        // @Valid triggers validation on the RegisterRequest DTO
        UserDto registeredUser = authService.register(registerRequest);

        // Return 201 Created status
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * POST /api/v1/auth/login
     * Endpoint for user login.
     *
     * @param loginRequest The request body containing email and password.
     * @return A ResponseEntity with the LoginResponse (tokens) and HTTP status 200.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
        @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * A test endpoint to verify JWT authentication.
     * It's secured because it's not under /api/v1/auth/**
     */
    @GetMapping("/me")
    public ResponseEntity<String> getAuthenticatedUserEmail(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // @AuthenticationPrincipal injects the UserDetails object we set in JwtAuthenticationFilter
        return ResponseEntity.ok("Your email is: " + userDetails.getUsername());
    }
}
