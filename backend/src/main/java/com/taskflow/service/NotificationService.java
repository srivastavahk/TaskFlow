package com.taskflow.service;

import com.taskflow.dto.NotificationDto;
import com.taskflow.entity.Notification;
import com.taskflow.entity.NotificationType;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.repository.NotificationRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Fetches all (unread and read) notifications for a user.
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(User user) {
        return notificationRepository
            .findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    /**
     * Creates notifications for a set of users.
     * This is the core helper method other services will call.
     */
    @Transactional
    public void createNotifications(
        Set<User> recipients,
        Task task,
        NotificationType type,
        String message
    ) {
        List<Notification> notifications = recipients
            .stream()
            .map(user ->
                Notification.builder()
                    .user(user)
                    // .task(task)
                    .type(type)
                    .message(message)
                    .isRead(false)
                    .taskId(task.getId())
                    .build()
            )
            .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
        // In a real app, this would also trigger an email
        // by pushing to a queue (e.g., SQS/RabbitMQ).
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .userId(notification.getUser().getId())
            .type(notification.getType())
            .message(notification.getMessage())
            .isRead(notification.isRead())
            .createdAt(notification.getCreatedAt())
            .taskId(notification.getTaskId())
            .build();
    }
}
