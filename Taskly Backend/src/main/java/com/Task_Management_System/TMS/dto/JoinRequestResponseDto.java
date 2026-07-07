package com.Task_Management_System.TMS.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestResponseDto {
    private UUID id;
    private UUID teamId;
    private String teamName;
    private UUID requesterId;
    private String requesterUsername;
    private UUID adminId;
    private String adminUsername;
    private String status;
    private String denialReason;
    private LocalDateTime createdAt;
}
