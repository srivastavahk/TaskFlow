package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    /**
     * Finds all tasks for a specific team, ordered by due date.
     */
    List<Task> findByTeamIdOrderByDueDateAsc(Long teamId);

    /**
     * Finds all tasks assigned to a specific user.
     */
    @Query("SELECT t FROM Task t JOIN t.assignees a WHERE a.id = :userId")
    List<Task> findByAssigneeId(Long userId);

    /**
     * Finds tasks for a team, filtered by status.
     */
    List<Task> findByTeamIdAndStatus(Long teamId, TaskStatus status);

    // Spring Data JPA will automatically parse method names to create queries
    // e.g., findByPriorityAndStatus, findByDueDateBefore, etc.
}
