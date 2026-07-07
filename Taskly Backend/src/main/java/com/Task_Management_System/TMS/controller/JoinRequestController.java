package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.DenyRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestResponseDto;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.service.JoinRequestService;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService joinRequestService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<JoinRequestResponseDto> createJoinRequest(
            @Valid @RequestBody JoinRequestDto dto,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        JoinRequestResponseDto response = joinRequestService.createJoinRequest(currentUser.getId(), dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<JoinRequestResponseDto> approveJoinRequest(
            @PathVariable UUID id,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        JoinRequestResponseDto response = joinRequestService.approveJoinRequest(currentUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deny")
    public ResponseEntity<JoinRequestResponseDto> denyJoinRequest(
            @PathVariable UUID id,
            @Valid @RequestBody DenyRequestDto dto,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        JoinRequestResponseDto response = joinRequestService.denyJoinRequest(currentUser.getId(), id, dto.getReason());
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current authenticated user not found"));
    }
}
