package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.AddMemberDto;
import com.Task_Management_System.TMS.dto.TeamDto;
import com.Task_Management_System.TMS.dto.TeamMemberDto;
import com.Task_Management_System.TMS.dto.TeamResponseDto;
import com.Task_Management_System.TMS.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Task_Management_System.TMS.exception.BadRequestException;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.UserRepository;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody TeamDto teamDto) {
        TeamResponseDto createdTeam = teamService.createTeam(teamDto);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponseDto> getTeamById(@PathVariable UUID id) {
        TeamResponseDto team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamResponseDto> updateTeam(
            @PathVariable UUID id,
            @Valid @RequestBody TeamDto teamDto) {
        TeamResponseDto updatedTeam = teamService.updateTeam(id, teamDto);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID id, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new com.Task_Management_System.TMS.exception.ResourceNotFoundException("Current user not found"));
        TeamResponseDto team = teamService.getTeamById(id);
        if (!team.getOwnerId().equals(currentUser.getId()) && !currentUser.isAdmin()) {
            throw new BadRequestException("Only the team owner or an administrator can delete this team");
        }
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<TeamMemberDto> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberDto addMemberDto) {
        TeamMemberDto member = teamService.addMember(id, addMemberDto);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        teamService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberDto>> getTeamMembers(@PathVariable UUID id) {
        List<TeamMemberDto> members = teamService.getTeamMembers(id);
        return ResponseEntity.ok(members);
    }
}
