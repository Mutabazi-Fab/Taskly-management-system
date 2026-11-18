package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.JoinRequestDto;
import com.Task_Management_System.TMS.exception.ConflictException;
import com.Task_Management_System.TMS.model.JoinRequest;
import com.Task_Management_System.TMS.model.Team;
import com.Task_Management_System.TMS.model.TeamMember;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.JoinRequestRepository;
import com.Task_Management_System.TMS.repository.NotificationRepository;
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
class JoinRequestServiceImplTest {

    @Mock private JoinRequestRepository joinRequestRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamMemberRepository teamMemberRepository;
    @Mock private NotificationService notificationService;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private JoinRequestServiceImpl service;

    private User admin;
    private User requester;
    private Team team;
    private JoinRequest pendingRequest;

    @BeforeEach
    void setUp() {
        admin = user("admin", true);
        requester = user("requester", false);
        team = Team.builder().id(UUID.randomUUID()).name("Alpha").owner(admin).build();
        pendingRequest = JoinRequest.builder()
                .id(UUID.randomUUID())
                .team(team)
                .requester(requester)
                .admin(admin)
                .status("PENDING")
                .build();
    }

    @Test
    void requesterCannotApproveTheirOwnRequest() {
        when(joinRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThrows(AccessDeniedException.class,
                () -> service.approveJoinRequest(requester.getId(), pendingRequest.getId()));

        verify(teamMemberRepository, never()).save(any());
        assertEquals("PENDING", pendingRequest.getStatus());
    }

    @Test
    void otherAdminCannotDenyARequestSentToSomeoneElse() {
        User otherAdmin = user("other-admin", true);
        when(joinRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThrows(AccessDeniedException.class,
                () -> service.denyJoinRequest(otherAdmin.getId(), pendingRequest.getId(), "No"));

        assertEquals("PENDING", pendingRequest.getStatus());
    }

    @Test
    void adminWhoWasDemotedCanNoLongerApprove() {
        admin.setAdmin(false);
        when(joinRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));

        assertThrows(AccessDeniedException.class,
                () -> service.approveJoinRequest(admin.getId(), pendingRequest.getId()));
    }

    @Test
    void addressedAdminCanApproveAndRequesterBecomesMember() {
        when(joinRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), requester.getId())).thenReturn(false);
        when(joinRequestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        service.approveJoinRequest(admin.getId(), pendingRequest.getId());

        verify(teamMemberRepository).save(any(TeamMember.class));
        assertEquals("APPROVED", pendingRequest.getStatus());
    }

    @Test
    void approvingSomeoneAlreadyInTheTeamDoesNotAddThemTwice() {
        when(joinRequestRepository.findById(pendingRequest.getId())).thenReturn(Optional.of(pendingRequest));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), requester.getId())).thenReturn(true);
        when(joinRequestRepository.save(pendingRequest)).thenReturn(pendingRequest);

        service.approveJoinRequest(admin.getId(), pendingRequest.getId());

        verify(teamMemberRepository, never()).save(any());
        assertEquals("APPROVED", pendingRequest.getStatus());
    }

    @Test
    void secondPendingRequestForSameTeamIsRejected() {
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(teamMemberRepository.existsByTeamIdAndUserId(team.getId(), requester.getId())).thenReturn(false);
        when(joinRequestRepository.existsByTeamIdAndRequesterIdAndStatus(team.getId(), requester.getId(), "PENDING"))
                .thenReturn(true);

        JoinRequestDto dto = JoinRequestDto.builder().teamId(team.getId()).adminId(admin.getId()).build();

        assertThrows(ConflictException.class, () -> service.createJoinRequest(requester.getId(), dto));
        verify(joinRequestRepository, never()).save(any());
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
