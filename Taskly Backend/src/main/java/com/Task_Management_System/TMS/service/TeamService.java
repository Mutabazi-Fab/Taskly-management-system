package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AddMemberDto;
import com.Task_Management_System.TMS.dto.TeamDto;
import com.Task_Management_System.TMS.dto.TeamMemberDto;
import com.Task_Management_System.TMS.dto.TeamResponseDto;
import com.Task_Management_System.TMS.model.User;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamResponseDto createTeam(TeamDto teamDto, User owner);
    List<TeamResponseDto> getAllTeams();
    TeamResponseDto getTeamById(UUID id);
    TeamResponseDto updateTeam(UUID id, TeamDto teamDto);
    void deleteTeam(UUID id);
    // actingUser = the logged-in user making the request, used for permission checks
    TeamMemberDto addMember(UUID teamId, AddMemberDto addMemberDto, User actingUser);
    void removeMember(UUID teamId, UUID userId, User actingUser);
    List<TeamMemberDto> getTeamMembers(UUID teamId);
}
