package com.taskflow.entity;

/**
 * Defines the status of a user account
 */
public enum UserStatus {
    /**
     * User has registered but not yet verified (if verification is implemented).
     * For MVP, we might just default to ACTIVE.
     */
    PENDING,

    /**
     * User is active and can log in.
     */
    ACTIVE,

    /**
     * User account is suspended and cannot log in.
     */
    SUSPENDED
}
