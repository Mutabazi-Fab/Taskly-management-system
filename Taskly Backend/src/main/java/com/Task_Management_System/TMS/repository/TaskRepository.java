package com.Task_Management_System.TMS.repository;

import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByTaskListId(UUID listId);
    List<Task> findByAssigneeId(UUID assigneeId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByTaskListBoardId(UUID boardId);
    List<Task> findByCreatorId(UUID creatorId);
}
