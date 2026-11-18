package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.NotificationResponseDto;
import com.Task_Management_System.TMS.security.CurrentUserProvider;
import com.Task_Management_System.TMS.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications() {
        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(currentUserId());
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable UUID id) {
        notificationService.markAsRead(currentUserId(), id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsRead() {
        notificationService.markAllAsRead(currentUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-read")
    public ResponseEntity<Void> clearAllReadNotifications() {
        notificationService.clearAllRead(currentUserId());
        return ResponseEntity.ok().build();
    }

    private UUID currentUserId() {
        return currentUserProvider.getCurrentUser().getId();
    }
}
