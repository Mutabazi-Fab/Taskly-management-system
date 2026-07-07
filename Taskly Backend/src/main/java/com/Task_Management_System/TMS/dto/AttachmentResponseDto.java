package com.Task_Management_System.TMS.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDto {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private UUID taskId;
    private LocalDateTime createdAt;
}
