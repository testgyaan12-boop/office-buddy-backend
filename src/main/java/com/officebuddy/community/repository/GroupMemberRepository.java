package com.officebuddy.community.repository;

import com.officebuddy.community.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByGroupIdOrderByCreatedAtDesc(UUID groupId);
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
    List<GroupMember> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, String status);
    boolean existsByGroupIdAndUserIdAndStatus(UUID groupId, UUID userId, String status);
    long countByGroupIdAndStatus(UUID groupId, String status);
}
