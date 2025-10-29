-- Team Invitations
-- Flyway Migration: V4
-- Creates the 'invitations' table

CREATE TABLE invitations
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL,
    team_id     BIGINT       NOT NULL,
    invited_by  BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP    NOT NULL,

    FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by) REFERENCES users (user_id),
    INDEX idx_invitation_token (token),
    INDEX idx_invitation_email (email)
) ENGINE = InnoDB;
