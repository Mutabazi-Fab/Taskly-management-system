package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.AttachmentDto;
import com.Task_Management_System.TMS.dto.AttachmentResponseDto;
import com.Task_Management_System.TMS.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentResponseDto> addAttachment(
            @PathVariable UUID taskId,
            @Valid @RequestBody AttachmentDto attachmentDto) {
        AttachmentResponseDto createdAttachment = attachmentService.addAttachment(taskId, attachmentDto);
        return new ResponseEntity<>(createdAttachment, HttpStatus.CREATED);
    }

    @GetMapping("/api/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentResponseDto>> getAttachmentsByTask(@PathVariable UUID taskId) {
        List<AttachmentResponseDto> attachments = attachmentService.getAttachmentsByTask(taskId);
        return ResponseEntity.ok(attachments);
    }

    @DeleteMapping("/api/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
