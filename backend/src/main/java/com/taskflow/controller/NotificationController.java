package com.taskflow.controller;

import com.taskflow.dto.NotificationDto;
import com.taskflow.entity.User;
import com.taskflow.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/v1/notifications/me
     * Fetches all notifications for the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
        @AuthenticationPrincipal User currentUser
    ) {
        List<NotificationDto> notifications =
            notificationService.getNotificationsForUser(currentUser);
        return ResponseEntity.ok(notifications);
    }
}
