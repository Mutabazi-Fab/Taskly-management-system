package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.TaskPriority;
import com.Task_Management_System.TMS.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 1, max = 150, message = "Task title must not exceed 150 characters")
    private String title;

    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private UUID assigneeId;

    @NotNull(message = "Creator user ID is required")
    private UUID creatorId;
}
