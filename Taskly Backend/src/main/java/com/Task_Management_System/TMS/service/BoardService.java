package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.BoardDto;
import com.Task_Management_System.TMS.dto.BoardResponseDto;
import com.Task_Management_System.TMS.dto.BoardStatsDto;

import java.util.List;
import java.util.UUID;

public interface BoardService {
    BoardResponseDto createBoard(UUID teamId, BoardDto boardDto);
    List<BoardResponseDto> getBoardsByTeam(UUID teamId);
    BoardResponseDto getBoardById(UUID id);
    BoardResponseDto updateBoard(UUID id, BoardDto boardDto);
    void deleteBoard(UUID id);
    BoardStatsDto getBoardStats(UUID id);
}
