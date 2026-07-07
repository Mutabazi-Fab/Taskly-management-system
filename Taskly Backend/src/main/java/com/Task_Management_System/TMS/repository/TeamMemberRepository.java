package com.Task_Management_System.TMS.repository;

import com.Task_Management_System.TMS.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);
    List<TeamMember> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    void deleteByUserId(UUID userId);
    void deleteByTeamId(UUID teamId);
}
