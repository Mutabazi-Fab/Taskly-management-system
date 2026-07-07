package com.Task_Management_System.TMS.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListResponseDto {
    private UUID id;
    private String name;
    private Integer position;
    private UUID boardId;
}
