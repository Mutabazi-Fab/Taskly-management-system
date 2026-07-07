package com.Task_Management_System.TMS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {

    @NotBlank(message = "Board name is required")
    private String name;
}
