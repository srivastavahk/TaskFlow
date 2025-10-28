package com.taskflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for 409 Conflict errors.
 * Used when a resource (e.g., user with an email) already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT) // Tells Spring to return a 409 status
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
