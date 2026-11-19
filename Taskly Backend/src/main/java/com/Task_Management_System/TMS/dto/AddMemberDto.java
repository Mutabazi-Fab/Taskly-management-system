package com.Task_Management_System.TMS.dto;

import com.Task_Management_System.TMS.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMemberDto {

    // Members are added by email, which is what the "Add Team Member" form collects
    @NotBlank(message = "Member email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    @NotNull(message = "Role is required (OWNER or MEMBER)")
    private Role role;
}
