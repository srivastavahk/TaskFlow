package com.taskflow.service;

import com.taskflow.dto.CommentDto;
import com.taskflow.dto.CreateCommentRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.Comment;
import com.taskflow.entity.NotificationType;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserTeamRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserTeamRepository userTeamRepository;
    private final TaskService taskService; // For helper methods
    private final NotificationService notificationService;

    /**
     * Adds a comment to a task.
     */
    @Transactional
    public CommentDto addCommentToTask(
        Long taskId,
        CreateCommentRequest request,
        User user
    ) {
        // 1. Find the task
        Task task = taskRepository
            .findById(taskId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Task", "id", taskId)
            );

        // 2. Verify user is a member of the task's team
        verifyTeamMembership(task.getTeam().getId(), user.getId());

        // 3. Create and save the comment
        Comment comment = Comment.builder()
            .task(task)
            .user(user)
            .text(request.getText())
            .build();

        Comment savedComment = commentRepository.save(comment);

        // --- Notification Logic ---
        // 2. Notify all assignees + the task creator
        Set<User> toNotify = new HashSet<>(task.getAssignees());
        toNotify.add(task.getCreatedBy());
        toNotify.remove(user); // Don't notify the person who commented

        if (!toNotify.isEmpty()) {
            notificationService.createNotifications(
                toNotify,
                task,
                NotificationType.NEW_COMMENT,
                user.getName() + " commented on: " + task.getTitle()
            );
        }
        // TODO: Add @mention parsing here
        // --- End Notification Logic ---

        return mapCommentToDto(savedComment);
    }

    /**
     * Gets all comments for a specific task.
     */
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForTask(Long taskId, User user) {
        // 1. Find the task
        Task task = taskRepository
            .findById(taskId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Task", "id", taskId)
            );

        // 2. Verify user is a member of the task's team
        verifyTeamMembership(task.getTeam().getId(), user.getId());

        // 3. Fetch and map comments
        List<Comment> comments =
            commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        return comments
            .stream()
            .map(this::mapCommentToDto)
            .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private void verifyTeamMembership(Long teamId, Long userId) {
        if (
            !userTeamRepository
                .findByUserIdAndTeamId(userId, teamId)
                .isPresent()
        ) {
            throw new AccessDeniedException(
                "User is not a member of this team"
            );
        }
    }

    private CommentDto mapCommentToDto(Comment comment) {
        return CommentDto.builder()
            .commentId(comment.getId())
            .taskId(comment.getTask().getId())
            .text(comment.getText())
            .createdAt(comment.getCreatedAt())
            .user(taskService.mapUserToDto(comment.getUser())) // Reuse mapper from TaskService
            .build();
    }
}
