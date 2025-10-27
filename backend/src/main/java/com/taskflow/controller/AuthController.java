package com.taskflow.controller;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // We will add the /login endpoint here in later
}
