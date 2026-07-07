package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String email;
    private Role role;
}
