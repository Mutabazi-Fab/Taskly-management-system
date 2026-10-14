package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AddMemberDto;
import com.Task_Management_System.TMS.dto.TeamDto;
import com.Task_Management_System.TMS.dto.TeamMemberDto;
import com.Task_Management_System.TMS.dto.TeamResponseDto;
import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.exception.ConflictException;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.*;
import com.Task_Management_System.TMS.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;
    private final AttachmentRepository attachmentRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public TeamResponseDto createTeam(TeamDto teamDto) {
        User owner = userRepository.findById(teamDto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + teamDto.getOwnerId()));

        if (owner.getAdmin() == null || !owner.getAdmin()) {
            throw new BadRequestException("Only administrators are allowed to create teams.");
        }

        Team team = Team.builder()
                .name(teamDto.getName())
                .owner(owner)
                .build();

        Team savedTeam = teamRepository.save(team);

        // Add owner to TeamMembers as OWNER
        TeamMember ownerMember = TeamMember.builder()
                .team(savedTeam)
                .user(owner)
                .role(Role.OWNER)
                .build();
        teamMemberRepository.save(ownerMember);

        return mapToResponseDto(savedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponseDto> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponseDto getTeamById(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return mapToResponseDto(team);
    }

    @Override
    @Transactional
    public TeamResponseDto updateTeam(UUID id, TeamDto teamDto) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        User owner = userRepository.findById(teamDto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + teamDto.getOwnerId()));

        team.setName(teamDto.getName());
        team.setOwner(owner);

        // Ensure owner is a member of the team and has OWNER role
        if (!teamMemberRepository.existsByTeamIdAndUserId(team.getId(), owner.getId())) {
            TeamMember ownerMember = TeamMember.builder()
                    .team(team)
                    .user(owner)
                    .role(Role.OWNER)
                    .build();
            teamMemberRepository.save(ownerMember);
        } else {
            TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(team.getId(), owner.getId()).get();
            if (membership.getRole() != Role.OWNER) {
                membership.setRole(Role.OWNER);
                teamMemberRepository.save(membership);
            }
        }

        Team updatedTeam = teamRepository.save(team);
        return mapToResponseDto(updatedTeam);
    }

    @Override
    @Transactional
    public void deleteTeam(UUID id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        // 1. Delete notifications related to join requests for this team, then delete join requests
        List<JoinRequest> joinRequests = joinRequestRepository.findByTeamId(id);
        for (JoinRequest req : joinRequests) {
            List<Notification> notifs = notificationRepository.findByRequestId(req.getId());
            if (!notifs.isEmpty()) {
                notificationRepository.deleteAll(notifs);
            }
        }
        if (!joinRequests.isEmpty()) {
            joinRequestRepository.deleteAll(joinRequests);
        }

        // 2. Cascade delete boards, lists, tasks, and attachments
        List<Board> boards = boardRepository.findByTeamId(id);
        for (Board board : boards) {
            List<TaskList> lists = taskListRepository.findByBoardIdOrderByPositionAsc(board.getId());
            for (TaskList list : lists) {
                List<Task> tasks = taskRepository.findByTaskListId(list.getId());
                for (Task task : tasks) {
                    List<Attachment> attachments = attachmentRepository.findByTaskId(task.getId());
                    if (!attachments.isEmpty()) {
                        attachmentRepository.deleteAll(attachments);
                    }
                }
                if (!tasks.isEmpty()) {
                    taskRepository.deleteAll(tasks);
                }
            }
            if (!lists.isEmpty()) {
                taskListRepository.deleteAll(lists);
            }
        }
        if (!boards.isEmpty()) {
            boardRepository.deleteAll(boards);
        }

        // 3. Delete all team members
        List<TeamMember> members = teamMemberRepository.findByTeamId(id);
        if (!members.isEmpty()) {
            teamMemberRepository.deleteAll(members);
        }

        // 4. Delete the team itself
        teamRepository.delete(team);
    }

    @Override
    @Transactional
    public TeamMemberDto addMember(UUID teamId, AddMemberDto addMemberDto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        User user = userRepository.findById(addMemberDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + addMemberDto.getUserId()));

        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, addMemberDto.getUserId())) {
            throw new ConflictException("User is already a member of this team");
        }

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .role(addMemberDto.getRole())
                .build();

        TeamMember savedMember = teamMemberRepository.save(teamMember);
        return mapToMemberDto(savedMember);
    }

    @Override
    @Transactional
    public void removeMember(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user: " + userId + " in team: " + teamId));

        if (membership.getRole() == Role.OWNER) {
            // Check if there are other owners
            List<TeamMember> owners = teamMemberRepository.findByTeamId(teamId).stream()
                    .filter(m -> m.getRole() == Role.OWNER)
                    .collect(Collectors.toList());
            if (owners.size() <= 1) {
                throw new BadRequestException("Cannot remove the sole owner of the team: " + teamId);
            }
        }

        teamMemberRepository.delete(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDto> getTeamMembers(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toList());
    }

    private TeamResponseDto mapToResponseDto(Team team) {
        return TeamResponseDto.builder()
                .id(team.getId())
                .name(team.getName())
                .ownerId(team.getOwner().getId())
                .ownerUsername(team.getOwner().getUsername())
                .createdAt(team.getCreatedAt())
                .build();
    }

    private TeamMemberDto mapToMemberDto(TeamMember member) {
        return TeamMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .build();
    }
}
