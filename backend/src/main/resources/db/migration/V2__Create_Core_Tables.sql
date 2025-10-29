-- Core Entities
-- Flyway Migration: V2
-- Creates tables for teams, tasks, comments, and relationships

-- Table: teams
CREATE TABLE teams
(
    team_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_by  BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Table: user_team (Join table for User <-> Team with roles)
CREATE TABLE user_team
(
    user_id   BIGINT      NOT NULL,
    team_id   BIGINT      NOT NULL,
    role      VARCHAR(32) NOT NULL COMMENT 'ROLE_ADMIN, ROLE_MEMBER, ROLE_VIEWER',
    joined_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, team_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE = InnoDB;

-- Table: tasks
CREATE TABLE tasks
(
    task_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id     BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(32)  NOT NULL COMMENT 'TODO, IN_PROGRESS, REVIEW, DONE, ARCHIVED',
    priority    VARCHAR(16)  NOT NULL COMMENT 'LOW, MEDIUM, HIGH, CRITICAL',
    due_date    TIMESTAMP NULL,
    created_by  BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    archived    BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (team_id) REFERENCES teams (team_id),
    FOREIGN KEY (created_by) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Index for dashboard queries (PRD Sec 7.3)
CREATE INDEX idx_task_team_status ON tasks (team_id, status);
CREATE INDEX idx_task_due_date ON tasks (due_date);

-- Table: task_assignees (Join table for Task <-> User)
CREATE TABLE task_assignees
(
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, user_id),
    FOREIGN KEY (task_id) REFERENCES tasks (task_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB;

-- Index for finding user's tasks (PRD Sec 7.3)
CREATE INDEX idx_assignee_user ON task_assignees (user_id);

-- Table: comments
CREATE TABLE comments
(
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id    BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks (task_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE = InnoDB;
