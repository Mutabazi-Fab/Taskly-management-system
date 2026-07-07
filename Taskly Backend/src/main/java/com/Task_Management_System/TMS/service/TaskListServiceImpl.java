package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.TaskListDto;
import com.Task_Management_System.TMS.dto.TaskListResponseDto;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.Board;
import com.Task_Management_System.TMS.model.TaskList;
import com.Task_Management_System.TMS.repository.BoardRepository;
import com.Task_Management_System.TMS.repository.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public TaskListResponseDto createTaskList(UUID boardId, TaskListDto listDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));

        TaskList taskList = TaskList.builder()
                .name(listDto.getName())
                .position(listDto.getPosition())
                .board(board)
                .build();

        TaskList savedList = taskListRepository.save(taskList);
        return mapToResponseDto(savedList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskListResponseDto> getTaskListsByBoard(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("Board not found with id: " + boardId);
        }
        return taskListRepository.findByBoardIdOrderByPositionAsc(boardId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskListResponseDto getTaskListById(UUID id) {
        TaskList taskList = taskListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList not found with id: " + id));
        return mapToResponseDto(taskList);
    }

    @Override
    @Transactional
    public TaskListResponseDto updateTaskList(UUID id, TaskListDto listDto) {
        TaskList taskList = taskListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList not found with id: " + id));

        taskList.setName(listDto.getName());
        taskList.setPosition(listDto.getPosition());

        TaskList updatedList = taskListRepository.save(taskList);
        return mapToResponseDto(updatedList);
    }

    @Override
    @Transactional
    public void deleteTaskList(UUID id) {
        TaskList taskList = taskListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList not found with id: " + id));
        
        // Hooks for deleting tasks within list will be automatically handled by CascadeType.ALL on tasks collection in Phase 4
        taskListRepository.delete(taskList);
    }

    private TaskListResponseDto mapToResponseDto(TaskList taskList) {
        return TaskListResponseDto.builder()
                .id(taskList.getId())
                .name(taskList.getName())
                .position(taskList.getPosition())
                .boardId(taskList.getBoard().getId())
                .build();
    }
}
