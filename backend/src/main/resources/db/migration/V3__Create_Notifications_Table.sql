-- Notification Entity
-- Flyway Migration: V3
-- Creates the 'notifications' table

CREATE TABLE notifications
(
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    type            VARCHAR(32)  NOT NULL COMMENT 'e.g., TASK_ASSIGNED, NEW_COMMENT',
    message         TEXT         NOT NULL,
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    task_id         BIGINT NULL,

    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (task_id) REFERENCES tasks (task_id),

    INDEX idx_notification_user (user_id)
) ENGINE = InnoDB;
