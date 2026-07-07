package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {

    @NotBlank(message = "Team name is required")
    private String name;

    @NotNull(message = "Owner user ID is required")
    private UUID ownerId;
}
