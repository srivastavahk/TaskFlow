package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
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
public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    @FutureOrPresent(message = "Due date must be in the present or future")
    private LocalDateTime dueDate;

    private Set<Long> assigneeIds;
    // private Set<String> tags;
    private Boolean archived;
}
