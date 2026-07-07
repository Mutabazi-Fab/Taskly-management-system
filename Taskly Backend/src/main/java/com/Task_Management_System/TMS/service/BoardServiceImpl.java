package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.BoardDto;
import com.Task_Management_System.TMS.dto.BoardResponseDto;
import com.Task_Management_System.TMS.dto.BoardStatsDto;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.Board;
import com.Task_Management_System.TMS.model.Team;
import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.model.TaskStatus;
import com.Task_Management_System.TMS.repository.BoardRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final TeamRepository teamRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public BoardResponseDto createBoard(UUID teamId, BoardDto boardDto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        Board board = Board.builder()
                .name(boardDto.getName())
                .team(team)
                .build();

        Board savedBoard = boardRepository.save(board);
        return mapToResponseDto(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponseDto> getBoardsByTeam(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return boardRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponseDto getBoardById(UUID id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        return mapToResponseDto(board);
    }

    @Override
    @Transactional
    public BoardResponseDto updateBoard(UUID id, BoardDto boardDto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setName(boardDto.getName());
        Board updatedBoard = boardRepository.save(board);
        return mapToResponseDto(updatedBoard);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        
        // Hooks for deleting task lists will be automatically handled by CascadeType.ALL on lists collection
        boardRepository.delete(board);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardStatsDto getBoardStats(UUID id) {
        if (!boardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }

        List<Task> tasks = taskRepository.findByTaskListBoardId(id);
        long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        return BoardStatsDto.builder()
                .todoCount(todo)
                .inProgressCount(inProgress)
                .doneCount(done)
                .build();
    }

    private BoardResponseDto mapToResponseDto(Board board) {
        return BoardResponseDto.builder()
                .id(board.getId())
                .name(board.getName())
                .teamId(board.getTeam().getId())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
