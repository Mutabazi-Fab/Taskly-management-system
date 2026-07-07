package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AttachmentDto;
import com.Task_Management_System.TMS.dto.AttachmentResponseDto;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    AttachmentResponseDto addAttachment(UUID taskId, AttachmentDto attachmentDto);
    List<AttachmentResponseDto> getAttachmentsByTask(UUID taskId);
    void deleteAttachment(UUID id);
}
