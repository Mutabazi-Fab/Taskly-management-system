package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.TaskDto;
import com.Task_Management_System.TMS.dto.TaskMoveDto;
import com.Task_Management_System.TMS.dto.TaskResponseDto;
import com.Task_Management_System.TMS.model.TaskStatus;
import com.Task_Management_System.TMS.model.User;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponseDto createTask(UUID listId, TaskDto taskDto, User creator);
    List<TaskResponseDto> getTasksByList(UUID listId);
    TaskResponseDto getTaskById(UUID id);
    TaskResponseDto updateTask(UUID id, TaskDto taskDto);
    TaskResponseDto updateTaskStatus(UUID id, TaskStatus status);
    TaskResponseDto moveTask(UUID id, TaskMoveDto moveDto);
    void deleteTask(UUID id);
    List<TaskResponseDto> getTasks(UUID assigneeId, TaskStatus status);
}
