package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {

    @NotBlank(message = "Board name is required")
    @Size(min = 1, max = 80, message = "Board name must not exceed 80 characters")
    private String name;
}
