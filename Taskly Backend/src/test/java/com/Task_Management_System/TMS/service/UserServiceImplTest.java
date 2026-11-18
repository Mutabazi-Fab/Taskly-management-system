package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.UserUpdateDto;
import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private User alice;
    private User bob;
    private User admin;

    @BeforeEach
    void setUp() {
        alice = user("alice", false);
        bob = user("bob", false);
        admin = user("admin", true);
    }

    // --- Editing ---

    @Test
    void userCannotEditSomeoneElsesAccount() {
        assertThrows(AccessDeniedException.class,
                () -> service.updateUser(bob.getId(), updateFor(bob, "hacked"), alice));

        verify(userRepository, never()).save(any());
    }

    @Test
    void userCanEditTheirOwnAccount() {
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateUser(alice.getId(), updateFor(alice, "alice-renamed"), alice);

        assertEquals("alice-renamed", alice.getUsername());
    }

    @Test
    void adminCanEditAnyAccount() {
        when(userRepository.findById(bob.getId())).thenReturn(Optional.of(bob));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateUser(bob.getId(), updateFor(bob, "bob-renamed"), admin);

        assertEquals("bob-renamed", bob.getUsername());
    }

    // --- Deleting ---

    @Test
    void userCannotDeleteSomeoneElsesAccount() {
        assertThrows(AccessDeniedException.class, () -> service.deleteUser(bob.getId(), alice));

        verify(userRepository, never()).delete(any());
    }

    @Test
    void userCanDeleteTheirOwnAccount() {
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(teamRepository.existsByOwnerId(alice.getId())).thenReturn(false);

        service.deleteUser(alice.getId(), alice);

        verify(userRepository).delete(alice);
    }

    @Test
    void adminCannotDeleteTheirOwnAccount() {
        assertThrows(BadRequestException.class, () -> service.deleteUser(admin.getId(), admin));

        verify(userRepository, never()).delete(any());
    }

    // --- Admin privileges ---

    @Test
    void adminCannotChangeTheirOwnAdminStatus() {
        assertThrows(BadRequestException.class, () -> service.toggleAdminStatus(admin.getId(), admin));

        verify(userRepository, never()).save(any());
    }

    @Test
    void regularUserCannotGrantAdminPrivileges() {
        assertThrows(AccessDeniedException.class, () -> service.toggleAdminStatus(alice.getId(), alice));

        assertFalse(alice.isAdmin());
    }

    private static UserUpdateDto updateFor(User user, String newUsername) {
        return UserUpdateDto.builder().username(newUsername).email(user.getEmail()).build();
    }

    private static User user(String name, boolean isAdmin) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(name)
                .email(name + "@example.com")
                .password("hashed")
                .admin(isAdmin)
                .build();
    }
}
