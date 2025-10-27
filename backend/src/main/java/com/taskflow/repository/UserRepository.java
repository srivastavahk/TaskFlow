package com.taskflow.repository;

import com.taskflow.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their email (which is unique).
     * This will be used by Spring Security's UserDetailsService.
     *
     * @param email The user's email address.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email The email to check.
     * @return true if an email exists, false otherwise.
     */
    boolean existsByEmail(String email);
}
