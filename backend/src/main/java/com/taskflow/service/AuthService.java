package com.taskflow.service;

import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.LoginResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.User;
import com.taskflow.entity.UserStatus;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for authentication-related business logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Helper method to map User entity to UserDto.
     */
    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .status(user.getStatus())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .build();
    }

    /**
     * Registers a new user in the system.
     *
     * @param request The registration request containing user details.
     * @return A DTO representing the newly created user.
     * @throws RuntimeException if the email is already taken.
     */
    @Transactional // Ensures this method runs within a single database transaction
    public UserDto register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            // We will replace this with a custom exception in the next step
            throw new DuplicateResourceException(
                "Email is already in use: " + request.getEmail()
            );
        }

        // 2. Create new user entity
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            // 3. Hash the password
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            // 4. Set default status
            .status(UserStatus.ACTIVE) // we'll default to ACTIVE
            .build();

        // 5. Save the user to the database
        User savedUser = userRepository.save(user);

        // 6. Map entity to DTO for the response
        // return UserDto.builder()
        //     .userId(savedUser.getId())
        //     .name(savedUser.getName())
        //     .email(savedUser.getEmail())
        //     .status(savedUser.getStatus())
        //     .avatarUrl(savedUser.getAvatarUrl())
        //     .createdAt(savedUser.getCreatedAt())
        //     .build();
        return mapUserToDto(savedUser); // Refactored to helper method
    }

    /**
     * Authenticates a user and returns a LoginResponse with JWTs.
     *
     * @param request The login request containing email and password.
     * @return A LoginResponse with tokens and user info.
     * @throws org.springframework.security.core.AuthenticationException if credentials are bad.
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Authenticate user with Spring Security
        // This will use our UserDetailsServiceImpl and PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 2. If authentication is successful, get the User object
        // The principal is the UserDetails object we returned from UserDetailsServiceImpl
        User user = (User) authentication.getPrincipal();

        // 3. Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Map user to DTO
        UserDto userDto = mapUserToDto(user);

        // 5. Build and return the response
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
            .user(userDto)
            .build();
    }
}
