package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.TaskListDto;
import com.Task_Management_System.TMS.dto.TaskListResponseDto;
import com.Task_Management_System.TMS.service.TaskListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskListController {

    private final TaskListService taskListService;

    @PostMapping("/api/boards/{boardId}/lists")
    public ResponseEntity<TaskListResponseDto> createTaskList(
            @PathVariable UUID boardId,
            @Valid @RequestBody TaskListDto listDto) {
        TaskListResponseDto createdList = taskListService.createTaskList(boardId, listDto);
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    @GetMapping("/api/boards/{boardId}/lists")
    public ResponseEntity<List<TaskListResponseDto>> getTaskListsByBoard(@PathVariable UUID boardId) {
        List<TaskListResponseDto> lists = taskListService.getTaskListsByBoard(boardId);
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/api/lists/{id}")
    public ResponseEntity<TaskListResponseDto> getTaskListById(@PathVariable UUID id) {
        TaskListResponseDto list = taskListService.getTaskListById(id);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/api/lists/{id}")
    public ResponseEntity<TaskListResponseDto> updateTaskList(
            @PathVariable UUID id,
            @Valid @RequestBody TaskListDto listDto) {
        TaskListResponseDto updatedList = taskListService.updateTaskList(id, listDto);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/api/lists/{id}")
    public ResponseEntity<Void> deleteTaskList(@PathVariable UUID id) {
        taskListService.deleteTaskList(id);
        return ResponseEntity.noContent().build();
    }
}
