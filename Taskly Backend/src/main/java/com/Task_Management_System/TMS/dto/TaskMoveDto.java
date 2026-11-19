package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/** Body for PATCH /api/tasks/{id}/move: which column to move the task to. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskMoveDto {

    @NotNull(message = "Target list ID is required")
    private UUID listId;

    // Optional: the new status that goes with the target column (e.g. DONE for "Done").
    // If omitted, the task keeps its current status.
    private TaskStatus status;
}
