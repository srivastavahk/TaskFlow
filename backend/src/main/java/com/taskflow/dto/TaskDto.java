package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long taskId;
    private Long teamId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<UserDto> assignees;
    // We will add commentsCount, attachments, and tags later
}
