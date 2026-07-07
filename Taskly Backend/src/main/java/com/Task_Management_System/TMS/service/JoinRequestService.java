package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.JoinRequestDto;
import com.Task_Management_System.TMS.dto.JoinRequestResponseDto;

import java.util.UUID;

public interface JoinRequestService {
    JoinRequestResponseDto createJoinRequest(UUID requesterId, JoinRequestDto dto);
    JoinRequestResponseDto approveJoinRequest(UUID adminId, UUID requestId);
    JoinRequestResponseDto denyJoinRequest(UUID adminId, UUID requestId, String reason);
}
