package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 80, message = "Team name must be between 2 and 80 characters")
    private String name;

    @NotNull(message = "Owner user ID is required")
    private UUID ownerId;
}
