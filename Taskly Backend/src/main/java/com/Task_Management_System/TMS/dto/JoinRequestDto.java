package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDto {

    @NotNull(message = "Team ID is required")
    private UUID teamId;

    @NotNull(message = "Administrator ID is required")
    private UUID adminId;
}
