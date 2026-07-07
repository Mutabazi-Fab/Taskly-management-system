package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.TaskListDto;
import com.Task_Management_System.TMS.dto.TaskListResponseDto;

import java.util.List;
import java.util.UUID;

public interface TaskListService {
    TaskListResponseDto createTaskList(UUID boardId, TaskListDto listDto);
    List<TaskListResponseDto> getTaskListsByBoard(UUID boardId);
    TaskListResponseDto getTaskListById(UUID id);
    TaskListResponseDto updateTaskList(UUID id, TaskListDto listDto);
    void deleteTaskList(UUID id);
}
