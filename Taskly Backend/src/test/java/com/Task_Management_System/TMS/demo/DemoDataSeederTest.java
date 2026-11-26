package com.Task_Management_System.TMS.demo;

import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.model.TaskStatus;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.BoardRepository;
import com.Task_Management_System.TMS.repository.TaskListRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private TaskListRepository taskListRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DemoDataSeeder seeder;

    @Test
    void doesNothingWhenTheDemoWorkspaceAlreadyExists() {
        when(userRepository.findByEmail(DemoAccount.EMAIL)).thenReturn(Optional.of(new User()));

        seeder.run(null);

        verify(userRepository, never()).save(any());
        verify(teamRepository, never()).save(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createsAWorkspaceWithTasksInEveryColumn() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(boardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskListRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        verify(userRepository, times(3)).save(any());
        verify(teamMemberRepository, times(3)).save(any());
        verify(taskListRepository, times(3)).save(any());

        ArgumentCaptor<Task> tasks = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(10)).save(tasks.capture());
        List<Task> saved = tasks.getAllValues();

        // Every column has work in it, and each task's status matches the column it sits in
        for (TaskStatus status : TaskStatus.values()) {
            assertTrue(saved.stream().anyMatch(t -> t.getStatus() == status), "no task with status " + status);
        }
        saved.forEach(t -> assertEquals(expectedStatus(t.getTaskList().getName()), t.getStatus(), t.getTitle()));

        // The demo user has work of their own, so My Tasks and the KPIs aren't empty
        assertTrue(saved.stream().anyMatch(t -> DemoAccount.EMAIL.equals(t.getAssignee().getEmail())));
    }

    private static TaskStatus expectedStatus(String listName) {
        return switch (listName) {
            case "To Do" -> TaskStatus.TODO;
            case "In Progress" -> TaskStatus.IN_PROGRESS;
            default -> TaskStatus.DONE;
        };
    }
}
