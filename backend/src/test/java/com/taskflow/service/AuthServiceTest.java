package com.taskflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.LoginResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.User;
import com.taskflow.entity.UserStatus;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for AuthService.
 * Uses Mockito to mock dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .name("Test User")
            .email("test@example.com")
            .password("password123")
            .build();

        user = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .status(UserStatus.ACTIVE)
            .build();
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(
            userRepository.existsByEmail(registerRequest.getEmail())
        ).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(
            "hashedPassword"
        );
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDto result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getName(), result.getName());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyInUse_ThrowsDuplicateResourceException() {
        // Arrange
        when(
            userRepository.existsByEmail(registerRequest.getEmail())
        ).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> authService.register(registerRequest)
        );

        assertEquals(
            "Email is already in use: test@example.com",
            exception.getMessage()
        );
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(
            "test@example.com",
            "password123"
        );
        Authentication authentication = mock(Authentication.class);

        when(
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            )
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationInSeconds()).thenReturn(900L);

        // Act
        LoginResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals(900L, result.getExpiresIn());
        assertEquals(user.getEmail(), result.getUser().getEmail());

        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void testLogin_BadCredentials_ThrowsBadCredentialsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(
            "test@example.com",
            "wrongpassword"
        );
        when(
            authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
            )
        ).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () ->
            authService.login(loginRequest)
        );

        verify(jwtService, never()).generateAccessToken(any(User.class));
    }
}
