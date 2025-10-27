-- User Entity
-- Flyway Migration: V1
-- Creates the 'users' table

CREATE TABLE users
(
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT 'Stored bcrypt hash',
    avatar_url    VARCHAR(255) NULL,
    status        VARCHAR(32)  NOT NULL COMMENT 'Enum: PENDING, ACTIVE, SUSPENDED',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- Index for fast email lookup
CREATE INDEX idx_user_email ON users (email);
