package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.UserRegistrationDto;
import com.Task_Management_System.TMS.dto.UserResponseDto;
import com.Task_Management_System.TMS.dto.UserUpdateDto;
import com.Task_Management_System.TMS.exception.ConflictException;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.JoinRequest;
import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.repository.JoinRequestRepository;
import com.Task_Management_System.TMS.repository.NotificationRepository;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto createUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new ConflictException("User with email " + registrationDto.getEmail() + " already exists");
        }

        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(UUID id, UserUpdateDto updateDto, User actingUser) {
        requireSelfOrAdmin(id, actingUser, "edit");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(updateDto.getEmail()) && userRepository.existsByEmail(updateDto.getEmail())) {
            throw new ConflictException("User with email " + updateDto.getEmail() + " already exists");
        }

        user.setUsername(updateDto.getUsername());
        user.setEmail(updateDto.getEmail());

        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id, User actingUser) {
        requireSelfOrAdmin(id, actingUser, "delete");

        // Admins can only be removed by another admin, so there is always at least one left
        if (actingUser.isAdmin() && actingUser.getId().equals(id)) {
            throw new BadRequestException("Administrators cannot delete their own account. Ask another administrator.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (teamRepository.existsByOwnerId(id)) {
            throw new BadRequestException("Cannot delete user as they own one or more teams. Transfer team ownership first.");
        }

        // Every table with a required link to this user must be cleaned up first,
        // or the database's foreign keys reject the delete.

        // 1. Team memberships
        teamMemberRepository.deleteByUserId(id);

        // 2. Tasks assigned to the user become unassigned. This goes through the task
        //    entities (not a bulk SQL UPDATE), so tasks Hibernate already has in memory
        //    are updated too and can't be flushed back still pointing at the deleted user.
        for (Task task : taskRepository.findByAssigneeId(id)) {
            task.setAssignee(null);
        }

        // 3. Tasks the user created are kept (the team still needs them) and handed to the
        //    owner of the team they belong to. A team always has an owner, and owners can't
        //    be deleted (checked above), so the new creator always exists.
        for (Task task : taskRepository.findByCreatorId(id)) {
            task.setCreator(task.getTaskList().getBoard().getTeam().getOwner());
        }

        // 4. Join requests the user sent, or that were sent to them as the deciding admin,
        //    plus the notifications that point at those requests
        List<JoinRequest> joinRequests = joinRequestRepository.findByRequesterIdOrAdminId(id, id);
        if (!joinRequests.isEmpty()) {
            notificationRepository.deleteByRequestIdIn(joinRequests.stream().map(JoinRequest::getId).toList());
            joinRequestRepository.deleteAll(joinRequests);
        }

        // 5. The user's own notifications
        notificationRepository.deleteByUserId(id);

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDto toggleAdminStatus(UUID id, User actingUser) {
        // SecurityConfig already limits this endpoint to admins; this is a second line of defence
        if (!actingUser.isAdmin()) {
            throw new AccessDeniedException("Only administrators can change administrator privileges");
        }
        // Stops an admin from locking themselves (and possibly everyone) out of admin tools
        if (actingUser.getId().equals(id)) {
            throw new BadRequestException("You cannot change your own administrator privileges");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setAdmin(!user.isAdmin());
        User updated = userRepository.save(user);
        return mapToResponseDto(updated);
    }

    /** Users may manage their own account; administrators may manage anyone's. */
    private void requireSelfOrAdmin(UUID targetUserId, User actingUser, String action) {
        if (!actingUser.getId().equals(targetUserId) && !actingUser.isAdmin()) {
            throw new AccessDeniedException("You can only " + action + " your own account");
        }
    }

    private UserResponseDto mapToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .admin(user.isAdmin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
