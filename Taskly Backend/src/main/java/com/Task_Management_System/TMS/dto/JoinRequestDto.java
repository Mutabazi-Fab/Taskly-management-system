package com.Task_Management_System.TMS.dto;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRequestDto {
    private UUID teamId;
    private UUID adminId;
}
