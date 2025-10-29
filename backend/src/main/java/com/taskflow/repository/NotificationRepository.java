package com.taskflow.repository;

import com.taskflow.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, Long> {
    /**
     * Finds all notifications for a specific user, ordered by most recent first.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
