package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.JoinRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestResponseDto;
import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.exception.ConflictException;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.*;
import com.Task_Management_System.TMS.repository.JoinRequestRepository;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinRequestServiceImpl implements JoinRequestService {

    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String DENIED = "DENIED";

    private final JoinRequestRepository joinRequestRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public JoinRequestResponseDto createJoinRequest(UUID requesterId, JoinRequestDto dto) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        User admin = userRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (!admin.isAdmin()) {
            throw new BadRequestException("Selected user is not an administrator");
        }

        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), requesterId)) {
            throw new ConflictException("You are already a member of this team");
        }

        if (joinRequestRepository.existsByTeamIdAndRequesterIdAndStatus(team.getId(), requesterId, PENDING)) {
            throw new ConflictException("You already have a pending request to join this team");
        }

        JoinRequest request = JoinRequest.builder()
                .team(team)
                .requester(requester)
                .admin(admin)
                .status(PENDING)
                .build();

        JoinRequest savedRequest = joinRequestRepository.save(request);

        // Notify the Admin
        String title = "Team Join Request";
        String message = requester.getUsername() + " requested to join " + team.getName();
        notificationService.createNotification(admin, title, message, "REQUEST", savedRequest.getId());

        return mapToResponseDto(savedRequest);
    }

    @Override
    @Transactional
    public JoinRequestResponseDto approveJoinRequest(UUID adminId, UUID requestId) {
        JoinRequest request = findPendingRequestForAdmin(requestId, adminId);

        // The requester may have been added to the team directly while the request was
        // waiting; adding them again would break the one-membership-per-team rule.
        boolean alreadyMember = teamMemberRepository.existsByTeamIdAndUserId(
                request.getTeam().getId(), request.getRequester().getId());
        if (!alreadyMember) {
            TeamMember newMember = TeamMember.builder()
                    .team(request.getTeam())
                    .user(request.getRequester())
                    .role(Role.MEMBER)
                    .build();
            teamMemberRepository.save(newMember);
        }

        request.setStatus(APPROVED);
        JoinRequest savedRequest = joinRequestRepository.save(request);
        markRequestNotificationsRead(requestId);

        // Notify the Requester
        String title = "Request Approved";
        String message = "Your request to join " + request.getTeam().getName() + " was approved.";
        notificationService.createNotification(request.getRequester(), title, message, "INFO", null);

        return mapToResponseDto(savedRequest);
    }

    @Override
    @Transactional
    public JoinRequestResponseDto denyJoinRequest(UUID adminId, UUID requestId, String reason) {
        JoinRequest request = findPendingRequestForAdmin(requestId, adminId);

        request.setStatus(DENIED);
        request.setDenialReason(reason);
        JoinRequest savedRequest = joinRequestRepository.save(request);
        markRequestNotificationsRead(requestId);

        // Notify the Requester
        String title = "Request Denied";
        String message = "Your request to join " + request.getTeam().getName() + " was denied. Reason: " + reason;
        notificationService.createNotification(request.getRequester(), title, message, "INFO", null);

        return mapToResponseDto(savedRequest);
    }

    /**
     * Loads a request and checks that the caller may decide on it: they must be the
     * administrator it was sent to, still be an administrator, and the request must
     * still be pending. Without this, any logged-in user could approve their own
     * request and join any team.
     */
    private JoinRequest findPendingRequestForAdmin(UUID requestId, UUID adminId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));

        User addressedAdmin = request.getAdmin();
        if (!addressedAdmin.getId().equals(adminId) || !addressedAdmin.isAdmin()) {
            throw new AccessDeniedException("Only the administrator this request was sent to can approve or deny it");
        }

        if (!PENDING.equals(request.getStatus())) {
            throw new BadRequestException("Request is already processed");
        }
        return request;
    }

    // Mark related request notifications as read so they disappear from the active list
    private void markRequestNotificationsRead(UUID requestId) {
        notificationRepository.findByRequestId(requestId).forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    private JoinRequestResponseDto mapToResponseDto(JoinRequest request) {
        return JoinRequestResponseDto.builder()
                .id(request.getId())
                .teamId(request.getTeam().getId())
                .teamName(request.getTeam().getName())
                .requesterId(request.getRequester().getId())
                .requesterUsername(request.getRequester().getUsername())
                .adminId(request.getAdmin().getId())
                .adminUsername(request.getAdmin().getUsername())
                .status(request.getStatus())
                .denialReason(request.getDenialReason())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
