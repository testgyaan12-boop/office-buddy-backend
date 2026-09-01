package com.officebuddy.community.repository;

import com.officebuddy.community.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    List<FriendRequest> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId);
    List<FriendRequest> findBySenderIdOrderByCreatedAtDesc(UUID senderId);
    Optional<FriendRequest> findBySenderIdAndReceiverId(UUID senderId, UUID receiverId);
    boolean existsBySenderIdAndReceiverIdAndStatus(UUID senderId, UUID receiverId, String status);
}
