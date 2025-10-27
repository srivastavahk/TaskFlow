package com.taskflow.config;

import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implements UserDetailsService to load user-specific data.
 * This service is used by Spring Security to authenticate a user.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the username (which is email in our case).
     */
    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {
        // Users log in with their email.
        return userRepository
            .findByEmail(username)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "User not found with email: " + username
                )
            );
    }
}
