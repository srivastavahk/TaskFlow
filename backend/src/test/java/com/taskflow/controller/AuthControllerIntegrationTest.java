package com.taskflow.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.LoginResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for AuthController.
 * Uses @SpringBootTest and MockMvc to test the full API flow.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev") // Use the 'dev' profile (which connects to our Docker DB)
@Transactional // Roll back database changes after each test
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clear the repository to ensure clean state
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .name("Integ Test User")
            .email("integ-test@example.com")
            .password("Password123!")
            .build();

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email", is(registerRequest.getEmail())))
            .andExpect(jsonPath("$.name", is(registerRequest.getName())))
            .andExpect(jsonPath("$.userId", notNullValue()));
    }

    @Test
    void testRegisterUser_ValidationFailure() throws Exception {
        RegisterRequest badRequest = RegisterRequest.builder()
            .name("No") // Too short (if we added @Size)
            .email("not-an-email") // Invalid format
            .password("short") // Too short
            .build();

        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(badRequest))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is("Validation failed")))
            .andExpect(
                jsonPath("$.validationErrors.email", is("Invalid email format"))
            )
            .andExpect(
                jsonPath(
                    "$.validationErrors.password",
                    is("Password must be at least 8 characters long")
                )
            );
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        // 1. Create the user first
        RegisterRequest registerRequest = RegisterRequest.builder()
            .name("Integ Test User")
            .email("integ-test@example.com")
            .password("Password123!")
            .build();

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        );

        // 2. Attempt to register again
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath(
                    "$.message",
                    is("Email is already in use: integ-test@example.com")
                )
            );
    }

    @Test
    void testLoginAndAccessSecureEndpoint_Success() throws Exception {
        // --- 1. Register User ---
        RegisterRequest registerRequest = RegisterRequest.builder()
            .name("Login Test User")
            .email("login-test@example.com")
            .password("Password123!")
            .build();

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        );

        // --- 2. Login ---
        LoginRequest loginRequest = new LoginRequest(
            registerRequest.getEmail(),
            registerRequest.getPassword()
        );

        MvcResult loginResult = mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", notNullValue()))
            .andExpect(jsonPath("$.user.email", is(loginRequest.getEmail())))
            .andReturn();

        // Extract access token
        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(
            responseBody,
            LoginResponse.class
        );
        String accessToken = loginResponse.getAccessToken();
        assertNotNull(accessToken);

        // --- 3. Access Secure Endpoint ---
        mockMvc
            .perform(
                get("/api/v1/auth/me").header(
                    "Authorization",
                    "Bearer " + accessToken
                )
            )
            .andExpect(status().isOk())
            .andExpect(
                content().string("Your email is: " + loginRequest.getEmail())
            );
    }

    @Test
    void testLogin_BadCredentials() throws Exception {
        // No user registered, so login will fail
        LoginRequest loginRequest = new LoginRequest(
            "no-user@example.com",
            "wrongpassword"
        );

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
            .andExpect(status().isUnauthorized()); // Or 403, Spring Security's default
    }

    @Test
    void testAccessSecureEndpoint_NoToken() throws Exception {
        // We tightened security in Step 9, this should now fail
        mockMvc
            .perform(get("/api/v1/auth/me"))
            .andExpect(status().isForbidden()); // Spring Security's default is 403 Forbidden
    }
}
