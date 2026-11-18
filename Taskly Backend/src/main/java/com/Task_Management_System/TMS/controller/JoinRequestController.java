package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.DenyRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestResponseDto;
import com.Task_Management_System.TMS.security.CurrentUserProvider;
import com.Task_Management_System.TMS.service.JoinRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService joinRequestService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<JoinRequestResponseDto> createJoinRequest(@Valid @RequestBody JoinRequestDto dto) {
        UUID requesterId = currentUserProvider.getCurrentUser().getId();
        JoinRequestResponseDto response = joinRequestService.createJoinRequest(requesterId, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<JoinRequestResponseDto> approveJoinRequest(@PathVariable UUID id) {
        UUID adminId = currentUserProvider.getCurrentUser().getId();
        JoinRequestResponseDto response = joinRequestService.approveJoinRequest(adminId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deny")
    public ResponseEntity<JoinRequestResponseDto> denyJoinRequest(
            @PathVariable UUID id,
            @Valid @RequestBody DenyRequestDto dto) {
        UUID adminId = currentUserProvider.getCurrentUser().getId();
        JoinRequestResponseDto response = joinRequestService.denyJoinRequest(adminId, id, dto.getReason());
        return ResponseEntity.ok(response);
    }
}
