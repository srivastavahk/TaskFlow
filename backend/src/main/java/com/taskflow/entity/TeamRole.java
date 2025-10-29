package com.taskflow.entity;

/**
 * Defines the role of a user within a specific team.
 */
public enum TeamRole {
    ROLE_ADMIN, // Can manage members, settings
    ROLE_MEMBER, // Can create/edit tasks
    ROLE_VIEWER, // Read-only (optional)
}
