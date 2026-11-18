package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenyRequestDto {

    // 255 matches the size of the denial_reason database column
    @NotBlank(message = "A reason for denial is required")
    @Size(max = 255, message = "Denial reason must not exceed 255 characters")
    private String reason;
}
