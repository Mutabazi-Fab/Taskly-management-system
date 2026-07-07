package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListDto {

    @NotBlank(message = "List name is required")
    private String name;

    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be 0 or positive")
    private Integer position;
}
