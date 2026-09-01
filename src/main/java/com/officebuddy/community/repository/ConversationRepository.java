package com.officebuddy.community.repository;

import com.officebuddy.community.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("SELECT c FROM Conversation c WHERE c.userAId = :userId OR c.userBId = :userId ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(UUID userId);

    @Query("SELECT c FROM Conversation c WHERE (c.userAId = :user1 AND c.userBId = :user2) OR (c.userAId = :user2 AND c.userBId = :user1)")
    Optional<Conversation> findByUsers(UUID user1, UUID user2);
}
