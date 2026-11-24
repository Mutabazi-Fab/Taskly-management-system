package com.Task_Management_System.TMS.repository;

import com.Task_Management_System.TMS.model.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JoinRequestRepository extends JpaRepository<JoinRequest, UUID> {
    List<JoinRequest> findByTeamId(UUID teamId);

    boolean existsByTeamIdAndRequesterIdAndStatus(UUID teamId, UUID requesterId, String status);

    // Every request the user sent, or that was sent to them as the deciding admin
    List<JoinRequest> findByRequesterIdOrAdminId(UUID requesterId, UUID adminId);
}
