package com.Task_Management_System.TMS.repository;

import com.Task_Management_System.TMS.model.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, UUID> {
    List<TaskList> findByBoardIdOrderByPositionAsc(UUID boardId);
}
