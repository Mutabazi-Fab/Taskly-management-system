package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.NotificationResponseDto;
import com.Task_Management_System.TMS.model.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationResponseDto> getUserNotifications(UUID userId);
    void markAsRead(UUID userId, UUID notificationId);
    void createNotification(User user, String title, String message, String type, UUID requestId);
}
