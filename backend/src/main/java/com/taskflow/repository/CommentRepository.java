package com.taskflow.repository;

import com.taskflow.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comment entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * Finds all comments for a specific task, ordered by creation date.
     */
    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
