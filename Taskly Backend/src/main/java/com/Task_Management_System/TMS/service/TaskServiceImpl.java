package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.TaskDto;
import com.Task_Management_System.TMS.dto.TaskResponseDto;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.*;
import com.Task_Management_System.TMS.repository.TaskListRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskResponseDto createTask(UUID listId, TaskDto taskDto) {
        TaskList taskList = taskListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList not found with id: " + listId));

        User creator = userRepository.findById(taskDto.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator User not found with id: " + taskDto.getCreatorId()));

        User assignee = null;
        if (taskDto.getAssigneeId() != null) {
            assignee = userRepository.findById(taskDto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee User not found with id: " + taskDto.getAssigneeId()));
        }

        TaskStatus status = taskDto.getStatus() != null ? taskDto.getStatus() : TaskStatus.TODO;
        TaskPriority priority = taskDto.getPriority() != null ? taskDto.getPriority() : TaskPriority.MEDIUM;

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(status)
                .priority(priority)
                .dueDate(taskDto.getDueDate())
                .taskList(taskList)
                .assignee(assignee)
                .creator(creator)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponseDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByList(UUID listId) {
        if (!taskListRepository.existsById(listId)) {
            throw new ResourceNotFoundException("TaskList not found with id: " + listId);
        }
        return taskRepository.findByTaskListId(listId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToResponseDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(UUID id, TaskDto taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User creator = userRepository.findById(taskDto.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator User not found with id: " + taskDto.getCreatorId()));

        User assignee = null;
        if (taskDto.getAssigneeId() != null) {
            assignee = userRepository.findById(taskDto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee User not found with id: " + taskDto.getAssigneeId()));
        }

        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        if (taskDto.getStatus() != null) {
            task.setStatus(taskDto.getStatus());
        }
        if (taskDto.getPriority() != null) {
            task.setPriority(taskDto.getPriority());
        }
        task.setDueDate(taskDto.getDueDate());
        task.setAssignee(assignee);
        task.setCreator(creator);

        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTaskStatus(UUID id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        // Hooks for deleting attachments nested under this task will be automatically handled by CascadeType.ALL on attachments collection in Phase 6
        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasks(UUID assigneeId, TaskStatus status) {
        List<Task> tasks;
        if (assigneeId != null && status != null) {
            tasks = taskRepository.findByAssigneeId(assigneeId).stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (assigneeId != null) {
            tasks = taskRepository.findByAssigneeId(assigneeId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private TaskResponseDto mapToResponseDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .listId(task.getTaskList().getId())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .creatorId(task.getCreator().getId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
