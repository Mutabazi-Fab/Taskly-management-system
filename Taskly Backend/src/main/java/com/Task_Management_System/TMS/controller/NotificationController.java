package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.NotificationResponseDto;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.service.NotificationService;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<NotificationResponseDto> notifications = notificationService.getUserNotifications(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable UUID id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current authenticated user not found"));
    }
}
