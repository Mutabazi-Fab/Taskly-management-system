package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.TaskMoveDto;
import com.Task_Management_System.TMS.dto.TaskResponseDto;
import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.model.*;
import com.Task_Management_System.TMS.repository.TaskListRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskListRepository taskListRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl service;

    private Board board;
    private TaskList todoList;
    private TaskList doneList;
    private Task task;

    @BeforeEach
    void setUp() {
        board = Board.builder().id(UUID.randomUUID()).name("Sprint 1").build();
        todoList = list("To Do", board);
        doneList = list("Done", board);

        User creator = User.builder().id(UUID.randomUUID()).username("jane").email("jane@example.com").password("x").build();
        task = Task.builder()
                .id(UUID.randomUUID())
                .title("Write report")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .taskList(todoList)
                .creator(creator)
                .build();
    }

    @Test
    void movingToAnotherColumnUpdatesListAndStatusTogether() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskListRepository.findById(doneList.getId())).thenReturn(Optional.of(doneList));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponseDto result = service.moveTask(task.getId(), new TaskMoveDto(doneList.getId(), TaskStatus.DONE));

        assertSame(doneList, task.getTaskList());
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(doneList.getId(), result.getListId());
    }

    @Test
    void movingWithoutStatusKeepsTheCurrentStatus() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskListRepository.findById(doneList.getId())).thenReturn(Optional.of(doneList));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.moveTask(task.getId(), new TaskMoveDto(doneList.getId(), null));

        assertSame(doneList, task.getTaskList());
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void cannotMoveTaskToAColumnOnAnotherBoard() {
        Board otherBoard = Board.builder().id(UUID.randomUUID()).name("Other team's board").build();
        TaskList foreignList = list("Done", otherBoard);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskListRepository.findById(foreignList.getId())).thenReturn(Optional.of(foreignList));

        assertThrows(BadRequestException.class,
                () -> service.moveTask(task.getId(), new TaskMoveDto(foreignList.getId(), TaskStatus.DONE)));

        assertSame(todoList, task.getTaskList());
        verify(taskRepository, never()).save(any());
    }

    private static TaskList list(String name, Board board) {
        return TaskList.builder().id(UUID.randomUUID()).name(name).position(0).board(board).build();
    }
}
