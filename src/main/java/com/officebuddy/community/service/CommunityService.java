package com.officebuddy.community.service;

import com.officebuddy.company.CompanyRepository;
import com.officebuddy.community.entity.*;
import com.officebuddy.community.repository.*;
import com.officebuddy.community.dto.*;
import com.officebuddy.user.User;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final FriendRequestRepository friendRequestRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OnlineStatusService onlineStatusService;
    private final CompanyRepository companyRepository;

    public List<FriendRequestDto> getIncomingRequests(UUID userId) {
        return friendRequestRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
                .filter(r -> r.getStatus().equals("PENDING"))
                .map(this::toFriendRequestDto)
                .collect(Collectors.toList());
    }

    public List<FriendRequestDto> getOutgoingRequests(UUID userId) {
        return friendRequestRepository.findBySenderIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toFriendRequestDto)
                .collect(Collectors.toList());
    }

    public FriendRequestDto sendRequest(UUID senderId, UUID receiverId) {
        if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, receiverId, "PENDING")) {
            throw new RuntimeException("Friend request already sent");
        }
        var request = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status("PENDING")
                .build();
        var saved = friendRequestRepository.save(request);
        return toFriendRequestDto(saved);
    }

    @Transactional
    public FriendRequestDto acceptRequest(UUID requestId, UUID userId) {
        var request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        request.setStatus("ACCEPTED");
        friendRequestRepository.save(request);
        return toFriendRequestDto(request);
    }

    @Transactional
    public void rejectRequest(UUID requestId, UUID userId) {
        var request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        if (!request.getReceiverId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        request.setStatus("REJECTED");
        friendRequestRepository.save(request);
    }

    public List<UUID> getFriendIds(UUID userId) {
        var sent = friendRequestRepository.findBySenderIdOrderByCreatedAtDesc(userId).stream()
                .filter(r -> r.getStatus().equals("ACCEPTED"))
                .map(FriendRequest::getReceiverId)
                .collect(Collectors.toList());
        var received = friendRequestRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
                .filter(r -> r.getStatus().equals("ACCEPTED"))
                .map(FriendRequest::getSenderId)
                .collect(Collectors.toList());
        sent.addAll(received);
        return sent;
    }

    public List<FriendDto> getFriends(UUID userId) {
        var friendIds = getFriendIds(userId);
        return friendIds.stream()
                .map(id -> userRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> FriendDto.builder()
                        .id(u.getId().toString())
                        .name(u.getName())
                        .email(u.getEmail())
                        .avatarUrl(u.getAvatarUrl())
                        .headline(u.getHeadline())
                        .skills(u.getSkills())
                        .online(onlineStatusService.isOnline(u.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<DiscoverUserDto> getDiscoverableUsers(UUID currentUserId) {
        var friendIds = getFriendIds(currentUserId);
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> {
                    String status = null;
                    if (friendIds.contains(u.getId())) {
                        status = "ACCEPTED";
                    } else {
                        var sentReq = friendRequestRepository.findBySenderIdAndReceiverId(currentUserId, u.getId());
                        var receivedReq = friendRequestRepository.findBySenderIdAndReceiverId(u.getId(), currentUserId);
                        if (sentReq.isPresent()) {
                            status = sentReq.get().getStatus();
                        } else if (receivedReq.isPresent()) {
                            status = receivedReq.get().getStatus().equals("PENDING") ? "RECEIVED_PENDING" : receivedReq.get().getStatus();
                        }
                    }
                    return DiscoverUserDto.builder()
                            .id(u.getId().toString())
                            .name(u.getName())
                            .email(u.getEmail())
                            .avatarUrl(u.getAvatarUrl())
                            .headline(u.getHeadline())
                            .currentCompany(u.getCurrentCompany())
                            .skills(u.getSkills())
                            .online(onlineStatusService.isOnline(u.getId()))
                            .friendStatus(status)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ConversationDto> getConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId).stream()
                .map(c -> {
                    var otherId = c.getUserAId().equals(userId) ? c.getUserBId() : c.getUserAId();
                    var otherUser = userRepository.findById(otherId).orElse(null);
                    var unread = messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(c.getId(), userId);
                    return ConversationDto.builder()
                            .id(c.getId().toString())
                            .otherUserId(otherId.toString())
                            .otherUserName(otherUser != null ? otherUser.getName() : "Unknown")
                            .otherUserAvatar(otherUser != null ? otherUser.getAvatarUrl() : null)
                            .lastMessage(c.getLastMessage())
                            .lastMessageAt(c.getLastMessageAt())
                            .online(onlineStatusService.isOnline(otherId))
                            .unreadCount(unread)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Conversation getOrCreateConversation(UUID user1, UUID user2) {
        return conversationRepository.findByUsers(user1, user2)
                .orElseGet(() -> {
                    var conv = Conversation.builder()
                            .userAId(user1)
                            .userBId(user2)
                            .build();
                    return conversationRepository.save(conv);
                });
    }

    @Transactional
    public MessageDto sendMessage(UUID senderId, UUID conversationId, String content, String type, String fileUrl) {
        var msg = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(content)
                .type(type != null ? type : "TEXT")
                .fileUrl(fileUrl)
                .build();
        var saved = messageRepository.save(msg);

        var conv = conversationRepository.findById(conversationId).orElse(null);
        if (conv != null) {
            conv.setLastMessage(content != null ? content : (type != null ? type : ""));
            conv.setLastMessageAt(LocalDateTime.now());
            conversationRepository.save(conv);
        }

        return toMessageDto(saved);
    }

    public List<MessageDto> getMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID conversationId, UUID userId) {
        var unread = messageRepository.findByConversationIdAndSenderIdNotAndReadAtIsNull(conversationId, userId);
        unread.forEach(m -> m.setReadAt(LocalDateTime.now()));
        messageRepository.saveAll(unread);
    }

    public List<CommunityCompanyDto> getUserCompanies(UUID userId) {
        return companyRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(c -> CommunityCompanyDto.builder()
                        .name(c.getName())
                        .role(c.getRole())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .isCurrent(c.isCurrent())
                        .build())
                .collect(Collectors.toList());
    }

    private FriendRequestDto toFriendRequestDto(FriendRequest r) {
        var sender = userRepository.findById(r.getSenderId()).orElse(null);
        var receiver = userRepository.findById(r.getReceiverId()).orElse(null);
        return FriendRequestDto.builder()
                .id(r.getId().toString())
                .senderId(r.getSenderId().toString())
                .senderName(sender != null ? sender.getName() : "Unknown")
                .senderAvatar(sender != null ? sender.getAvatarUrl() : null)
                .receiverId(r.getReceiverId().toString())
                .receiverName(receiver != null ? receiver.getName() : "Unknown")
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private MessageDto toMessageDto(Message m) {
        return MessageDto.builder()
                .id(m.getId().toString())
                .conversationId(m.getConversationId().toString())
                .senderId(m.getSenderId().toString())
                .content(m.getContent())
                .type(m.getType())
                .fileUrl(m.getFileUrl())
                .createdAt(m.getCreatedAt())
                .read(m.getReadAt() != null)
                .build();
    }
}
