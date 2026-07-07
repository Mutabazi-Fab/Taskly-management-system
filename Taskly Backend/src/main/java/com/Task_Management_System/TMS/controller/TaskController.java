package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.TaskDto;
import com.Task_Management_System.TMS.dto.TaskResponseDto;
import com.Task_Management_System.TMS.dto.TaskStatusUpdateDto;
import com.Task_Management_System.TMS.model.TaskStatus;
import com.Task_Management_System.TMS.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/lists/{listId}/tasks")
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable UUID listId,
            @Valid @RequestBody TaskDto taskDto) {
        TaskResponseDto createdTask = taskService.createTask(listId, taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/api/lists/{listId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksByList(@PathVariable UUID listId) {
        List<TaskResponseDto> tasks = taskService.getTasksByList(listId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDto taskDto) {
        TaskResponseDto updatedTask = taskService.updateTask(id, taskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/api/tasks/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody TaskStatusUpdateDto statusUpdateDto) {
        TaskResponseDto updatedTask = taskService.updateTaskStatus(id, statusUpdateDto.getStatus());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/api/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasks(
            @RequestParam(required = false) UUID assignee,
            @RequestParam(required = false) TaskStatus status) {
        List<TaskResponseDto> tasks = taskService.getTasks(assignee, status);
        return ResponseEntity.ok(tasks);
    }
}
