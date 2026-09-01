package com.officebuddy.community.repository;

import com.officebuddy.community.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
    long countByConversationIdAndReadAtIsNullAndSenderIdNot(UUID conversationId, UUID senderId);
    List<Message> findByConversationIdAndSenderIdNotAndReadAtIsNull(UUID conversationId, UUID senderId);
}
