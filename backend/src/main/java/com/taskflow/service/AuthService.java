package com.taskflow.service;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.User;
import com.taskflow.entity.UserStatus;
import com.taskflow.exception.DuplicateResourceException;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
        return UserDto.builder()
            .userId(savedUser.getId())
            .name(savedUser.getName())
            .email(savedUser.getEmail())
            .status(savedUser.getStatus())
            .avatarUrl(savedUser.getAvatarUrl())
            .createdAt(savedUser.getCreatedAt())
            .build();
    }
}
