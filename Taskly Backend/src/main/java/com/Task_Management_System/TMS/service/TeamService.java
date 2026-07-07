package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AddMemberDto;
import com.Task_Management_System.TMS.dto.TeamDto;
import com.Task_Management_System.TMS.dto.TeamMemberDto;
import com.Task_Management_System.TMS.dto.TeamResponseDto;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamResponseDto createTeam(TeamDto teamDto);
    List<TeamResponseDto> getAllTeams();
    TeamResponseDto getTeamById(UUID id);
    TeamResponseDto updateTeam(UUID id, TeamDto teamDto);
    void deleteTeam(UUID id);
    TeamMemberDto addMember(UUID teamId, AddMemberDto addMemberDto);
    void removeMember(UUID teamId, UUID userId);
    List<TeamMemberDto> getTeamMembers(UUID teamId);
}
