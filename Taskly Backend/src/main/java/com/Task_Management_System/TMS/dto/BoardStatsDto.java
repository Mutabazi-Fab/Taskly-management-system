package com.Task_Management_System.TMS.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardStatsDto {
    private long todoCount;
    private long inProgressCount;
    private long doneCount;
}
