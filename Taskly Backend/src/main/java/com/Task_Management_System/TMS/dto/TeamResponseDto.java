package com.Task_Management_System.TMS.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponseDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private String ownerUsername;
    private LocalDateTime createdAt;
}
