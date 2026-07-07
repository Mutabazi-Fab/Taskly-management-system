package com.Task_Management_System.TMS.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private UUID requestId;
    private LocalDateTime createdAt;
}
