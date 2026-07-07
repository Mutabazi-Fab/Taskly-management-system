package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.AttachmentDto;
import com.Task_Management_System.TMS.dto.AttachmentResponseDto;
import com.Task_Management_System.TMS.exception.ResourceNotFoundException;
import com.Task_Management_System.TMS.model.Attachment;
import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.repository.AttachmentRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public AttachmentResponseDto addAttachment(UUID taskId, AttachmentDto attachmentDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Attachment attachment = Attachment.builder()
                .fileName(attachmentDto.getFileName())
                .fileUrl(attachmentDto.getFileUrl())
                .task(task)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return mapToResponseDto(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAttachmentsByTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }
        return attachmentRepository.findByTaskId(taskId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
        attachmentRepository.delete(attachment);
    }

    private AttachmentResponseDto mapToResponseDto(Attachment attachment) {
        return AttachmentResponseDto.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileUrl(attachment.getFileUrl())
                .taskId(attachment.getTask().getId())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
