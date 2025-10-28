package com.taskflow.service;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskDto;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.dto.UserDto;
import com.taskflow.entity.Task;
import com.taskflow.entity.Team;
import com.taskflow.entity.User;
import com.taskflow.exception.AccessDeniedException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.TeamRepository;
import com.taskflow.repository.UserRepository;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserTeamRepository userTeamRepository;

    /**
     * Creates a new task.
     */
    @Transactional
    public TaskDto createTask(CreateTaskRequest request, User creator) {
        // 1. Validate team exists and user is a member
        Team team = getTeamAndVerifyMembership(
            request.getTeamId(),
            creator.getId()
        );

        // 2. Find assignee User entities
        Set<User> assignees = new HashSet<>();
        if (
            request.getAssigneeIds() != null &&
            !request.getAssigneeIds().isEmpty()
        ) {
            // In a real app, we should also verify assignees are part of the team
            assignees.addAll(
                userRepository.findAllById(request.getAssigneeIds())
            );
        }

        // 3. Create and save the task
        Task task = Task.builder()
            .team(team)
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus())
            .priority(request.getPriority())
            .dueDate(request.getDueDate())
            .createdBy(creator)
            .assignees(assignees)
            .build();

        Task savedTask = taskRepository.save(task);
        return mapTaskToDto(savedTask);
    }

    /**
     * Gets all tasks for a given team.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksForTeam(Long teamId, User user) {
        // 1. Validate user is a member of the team
        verifyTeamMembership(teamId, user.getId());

        // 2. Fetch and map tasks
        List<Task> tasks = taskRepository.findByTeamIdOrderByDueDateAsc(teamId);
        return tasks
            .stream()
            .map(this::mapTaskToDto)
            .collect(Collectors.toList());
    }

    /**
     * Gets a single task by its ID.
     */
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long taskId, User user) {
        // 1. Find the task
        Task task = taskRepository
            .findById(taskId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Task", "id", taskId)
            );

        // 2. Verify user is a member of the task's team
        verifyTeamMembership(task.getTeam().getId(), user.getId());

        // 3. Map and return
        return mapTaskToDto(task);
    }

    /**
     * Partially updates a task.
     */
    @Transactional
    public TaskDto updateTask(
        Long taskId,
        UpdateTaskRequest request,
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

        // 3. Apply partial updates (only non-null fields from request)
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getArchived() != null) {
            task.setArchived(request.getArchived());
        }
        if (request.getAssigneeIds() != null) {
            Set<User> assignees = new HashSet<>(
                userRepository.findAllById(request.getAssigneeIds())
            );
            task.setAssignees(assignees);
        }

        Task updatedTask = taskRepository.save(task);
        return mapTaskToDto(updatedTask);
    }

    /**
     * Deletes (soft-deletes) a task.
     */
    @Transactional
    public void deleteTask(Long taskId, User user) {
        Task task = taskRepository
            .findById(taskId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Task", "id", taskId)
            );

        // Verify membership
        verifyTeamMembership(task.getTeam().getId(), user.getId());

        // TODO: Add role check (e.g., only ADMIN or task creator can delete)

        // Soft delete
        task.setArchived(true);
        task.setStatus(com.taskflow.entity.TaskStatus.ARCHIVED);
        taskRepository.save(task);
    }

    // --- Helper & Security Methods ---

    private Team getTeamAndVerifyMembership(Long teamId, Long userId) {
        Team team = teamRepository
            .findById(teamId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Team", "id", teamId)
            );
        verifyTeamMembership(teamId, userId);
        return team;
    }

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

    private TaskDto mapTaskToDto(Task task) {
        return TaskDto.builder()
            .taskId(task.getId())
            .teamId(task.getTeam().getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .dueDate(task.getDueDate())
            .createdBy(mapUserToDto(task.getCreatedBy()))
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .assignees(
                task
                    .getAssignees()
                    .stream()
                    .map(this::mapUserToDto)
                    .collect(Collectors.toSet())
            )
            .build();
    }

    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
            .userId(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .status(user.getStatus())
            .avatarUrl(user.getAvatarUrl())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
