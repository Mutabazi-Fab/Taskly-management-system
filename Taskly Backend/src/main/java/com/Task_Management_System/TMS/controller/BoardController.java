package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.BoardDto;
import com.Task_Management_System.TMS.dto.BoardResponseDto;
import com.Task_Management_System.TMS.dto.BoardStatsDto;
import com.Task_Management_System.TMS.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/api/teams/{teamId}/boards")
    public ResponseEntity<BoardResponseDto> createBoard(
            @PathVariable UUID teamId,
            @Valid @RequestBody BoardDto boardDto) {
        BoardResponseDto createdBoard = boardService.createBoard(teamId, boardDto);
        return new ResponseEntity<>(createdBoard, HttpStatus.CREATED);
    }

    @GetMapping("/api/teams/{teamId}/boards")
    public ResponseEntity<List<BoardResponseDto>> getBoardsByTeam(@PathVariable UUID teamId) {
        List<BoardResponseDto> boards = boardService.getBoardsByTeam(teamId);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/api/boards/{id}")
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable UUID id) {
        BoardResponseDto board = boardService.getBoardById(id);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/api/boards/{id}")
    public ResponseEntity<BoardResponseDto> updateBoard(
            @PathVariable UUID id,
            @Valid @RequestBody BoardDto boardDto) {
        BoardResponseDto updatedBoard = boardService.updateBoard(id, boardDto);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable UUID id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/boards/{id}/stats")
    public ResponseEntity<BoardStatsDto> getBoardStats(@PathVariable UUID id) {
        BoardStatsDto stats = boardService.getBoardStats(id);
        return ResponseEntity.ok(stats);
    }
}
