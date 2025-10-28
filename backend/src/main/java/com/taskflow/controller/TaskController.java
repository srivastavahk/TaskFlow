package com.taskflow.controller;

import com.taskflow.dto.CommentDto;
import com.taskflow.dto.CreateCommentRequest;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskDto;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.User;
import com.taskflow.service.CommentService;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Task Management endpoints.
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;

    /**
     * POST /api/v1/tasks
     * Creates a new task.
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
        @Valid @RequestBody CreateTaskRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        TaskDto createdTask = taskService.createTask(request, currentUser);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/tasks
     * Lists tasks. Requires a 'teamId' query parameter.
     */
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
        @RequestParam Long teamId, // Required param for now
        @AuthenticationPrincipal User currentUser
    ) {
        // We'll add more filters later (status, assignee, etc.)
        List<TaskDto> tasks = taskService.getTasksForTeam(teamId, currentUser);
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/v1/tasks/{id}
     * Gets a single task by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        TaskDto task = taskService.getTaskById(id, currentUser);
        return ResponseEntity.ok(task);
    }

    /**
     * PATCH /api/v1/tasks/{id}
     * Partially updates a task.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        TaskDto updatedTask = taskService.updateTask(id, request, currentUser);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * DELETE /api/v1/tasks/{id}
     * Soft-deletes (archives) a task.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Comment Endpoints ---

    /**
     * POST /api/v1/tasks/{id}/comments
     * Adds a comment to the specified task.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(
        @PathVariable Long id,
        @Valid @RequestBody CreateCommentRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        CommentDto comment = commentService.addCommentToTask(
            id,
            request,
            currentUser
        );
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/tasks/{id}/comments
     * Lists all comments for the specified task.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getComments(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        List<CommentDto> comments = commentService.getCommentsForTask(
            id,
            currentUser
        );
        return ResponseEntity.ok(comments);
    }
}
