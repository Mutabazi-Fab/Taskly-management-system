package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.TaskPriority;
import com.Task_Management_System.TMS.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private UUID assigneeId;

    @NotNull(message = "Creator user ID is required")
    private UUID creatorId;
}
