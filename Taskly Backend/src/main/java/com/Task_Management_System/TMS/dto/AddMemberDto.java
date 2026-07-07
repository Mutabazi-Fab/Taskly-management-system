package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMemberDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required (OWNER or MEMBER)")
    private Role role;
}
