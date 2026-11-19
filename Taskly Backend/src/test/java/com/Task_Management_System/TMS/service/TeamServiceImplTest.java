package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AddMemberDto;
import com.Task_Management_System.TMS.dto.TeamMemberDto;
import com.Task_Management_System.TMS.exception.ConflictException;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.Role;
import com.Task_Management_System.TMS.model.Team;
import com.Task_Management_System.TMS.model.TeamMember;
import com.Task_Management_System.TMS.model.User;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock private TeamRepository teamRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl service;

    private User owner;
    private User member;
    private User newcomer;
    private Team team;

    @BeforeEach
    void setUp() {
        owner = user("owner", false);
        member = user("member", false);
        newcomer = user("newcomer", false);
        team = Team.builder().id(UUID.randomUUID()).name("Alpha").owner(owner).build();
    }

    // --- Adding members ---

    @Test
    void ownerCanAddMemberByEmail() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(newcomer.getEmail())).thenReturn(Optional.of(newcomer));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamMemberDto added = service.addMember(team.getId(), addRequest(" " + newcomer.getEmail() + " "), owner);

        assertEquals(newcomer.getId(), added.getUserId());
        assertEquals(Role.MEMBER, added.getRole());
    }

    @Test
    void adminWhoIsNotInTheTeamCanStillAddMembers() {
        User admin = user("admin", true);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(newcomer.getEmail())).thenReturn(Optional.of(newcomer));
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addMember(team.getId(), addRequest(newcomer.getEmail()), admin);

        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    void regularMemberCannotAddMembers() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamIdAndUserId(team.getId(), member.getId()))
                .thenReturn(Optional.of(membership(member, Role.MEMBER)));

        assertThrows(AccessDeniedException.class,
                () -> service.addMember(team.getId(), addRequest(newcomer.getEmail()), member));

        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    void addingUnknownEmailGives404() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.addMember(team.getId(), addRequest("nobody@example.com"), owner));
    }

    @Test
    void addingExistingMemberGives409() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), member.getId())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> service.addMember(team.getId(), addRequest(member.getEmail()), owner));
    }

    // --- Removing members ---

    @Test
    void memberCanLeaveTheTeam() {
        TeamMember membership = membership(member, Role.MEMBER);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamIdAndUserId(team.getId(), member.getId())).thenReturn(Optional.of(membership));

        service.removeMember(team.getId(), member.getId(), member);

        verify(teamMemberRepository).delete(membership);
    }

    @Test
    void memberCannotRemoveSomeoneElse() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.findByTeamIdAndUserId(team.getId(), member.getId()))
                .thenReturn(Optional.of(membership(member, Role.MEMBER)));

        assertThrows(AccessDeniedException.class,
                () -> service.removeMember(team.getId(), newcomer.getId(), member));

        verify(teamMemberRepository, never()).delete(any());
    }

    private TeamMember membership(User user, Role role) {
        return TeamMember.builder().id(UUID.randomUUID()).team(team).user(user).role(role).build();
    }

    private static AddMemberDto addRequest(String email) {
        return AddMemberDto.builder().email(email).role(Role.MEMBER).build();
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
