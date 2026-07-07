package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.TaskPriority;
import com.Task_Management_System.TMS.model.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDto {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private UUID listId;
    private UUID assigneeId;
    private UUID creatorId;
    private LocalDateTime createdAt;
}
