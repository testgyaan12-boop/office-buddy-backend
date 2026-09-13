package com.officebuddy.community.repository;

import com.officebuddy.community.entity.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {
    List<ChatGroup> findByAdminIdOrderByCreatedAtDesc(UUID adminId);

    @Query("SELECT g FROM ChatGroup g WHERE g.id IN (SELECT m.groupId FROM GroupMember m WHERE m.userId = :userId AND m.status = 'APPROVED')")
    List<ChatGroup> findGroupsByMemberUserId(UUID userId);
}
