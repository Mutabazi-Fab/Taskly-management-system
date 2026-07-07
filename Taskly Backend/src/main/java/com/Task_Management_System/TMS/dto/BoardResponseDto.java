package com.Task_Management_System.TMS.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponseDto {
    private UUID id;
    private String name;
    private UUID teamId;
    private LocalDateTime createdAt;
}
