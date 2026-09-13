package com.officebuddy.admin;

import com.officebuddy.community.entity.Conversation;
import com.officebuddy.community.entity.FriendRequest;
import com.officebuddy.community.entity.Message;
import com.officebuddy.community.repository.ConversationRepository;
import com.officebuddy.community.repository.FriendRequestRepository;
import com.officebuddy.community.repository.MessageRepository;
import com.officebuddy.community.service.OnlineStatusService;
import com.officebuddy.storage.StorageService;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final OnlineStatusService onlineStatusService;
    private final StorageService storageService;

    @GetMapping("/online")
    public ResponseEntity<?> onlineUsers() {
        var ids = onlineStatusService.getOnlineUserIds();
        var list = new ArrayList<Map<String, Object>>();
        for (var uid : ids) {
            var user = userRepository.findById(uid).orElse(null);
            if (user != null) {
                var m = new LinkedHashMap<String, Object>();
                m.put("id", uid.toString());
                m.put("name", user.getName());
                m.put("email", user.getEmail());
                m.put("avatarUrl", user.getAvatarUrl());
                list.add(m);
            }
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/connections")
    public ResponseEntity<?> connections() {
        var accepted = friendRequestRepository.findAll().stream()
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .toList();
        var all = userRepository.findAll();
        var userMap = new HashMap<UUID, com.officebuddy.user.User>();
        for (var u : all) userMap.put(u.getId(), u);

        var enriched = new ArrayList<Map<String, Object>>();
        var seen = new HashSet<String>();
        for (var f : accepted) {
            var key = f.getSenderId().toString() + "-" + f.getReceiverId();
            var keyRev = f.getReceiverId().toString() + "-" + f.getSenderId();
            if (seen.contains(key) || seen.contains(keyRev)) continue;
            seen.add(key);

            var userA = userMap.get(f.getSenderId());
            var userB = userMap.get(f.getReceiverId());
            if (userA == null || userB == null) continue;

            var m = new LinkedHashMap<String, Object>();
            m.put("userAId", userA.getId().toString());
            m.put("userAName", userA.getName());
            m.put("userAEmail", userA.getEmail());
            m.put("userAAvatar", userA.getAvatarUrl());
            m.put("userAOnline", onlineStatusService.isOnline(userA.getId()));
            m.put("userBId", userB.getId().toString());
            m.put("userBName", userB.getName());
            m.put("userBEmail", userB.getEmail());
            m.put("userBAvatar", userB.getAvatarUrl());
            m.put("userBOnline", onlineStatusService.isOnline(userB.getId()));
            enriched.add(m);
        }
        return ResponseEntity.ok(enriched);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> conversations() {
        var convos = conversationRepository.findAll();
        var enriched = new ArrayList<Map<String, Object>>();
        for (var c : convos) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", c.getId().toString());
            m.put("lastMessage", c.getLastMessage());
            m.put("lastMessageAt", c.getLastMessageAt() != null ? c.getLastMessageAt().toString() : null);
            m.put("userAId", c.getUserAId().toString());
            m.put("userBId", c.getUserBId().toString());
            var userA = userRepository.findById(c.getUserAId()).orElse(null);
            var userB = userRepository.findById(c.getUserBId()).orElse(null);
            if (userA != null) {
                m.put("userAName", userA.getName());
                m.put("userAEmail", userA.getEmail());
                m.put("userAAvatar", userA.getAvatarUrl());
            }
            if (userB != null) {
                m.put("userBName", userB.getName());
                m.put("userBEmail", userB.getEmail());
                m.put("userBAvatar", userB.getAvatarUrl());
            }
            m.put("userAOnline", onlineStatusService.isOnline(c.getUserAId()));
            m.put("userBOnline", onlineStatusService.isOnline(c.getUserBId()));
            enriched.add(m);
        }
        enriched.sort((a, b) -> {
            String la = (String) a.get("lastMessageAt");
            String lb = (String) b.get("lastMessageAt");
            if (la == null && lb == null) return 0;
            if (la == null) return 1;
            if (lb == null) return -1;
            return lb.compareTo(la);
        });
        return ResponseEntity.ok(enriched);
    }

    @PostMapping("/conversations/{id}/send")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        if (!"1234".equals(adminKey)) {
            return ResponseEntity.status(403).body(Map.of("message", "Invalid admin key"));
        }
        String content = body.getOrDefault("content", "");
        String type = body.getOrDefault("type", "TEXT");
        if ((content == null || content.isBlank()) && "TEXT".equals(type)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content required"));
        }
        var admins = userRepository.findByEmail("admin@officebuddy.app");
        if (admins.isEmpty()) return ResponseEntity.status(500).body(Map.of("message", "Admin not found"));
        UUID adminId = admins.get().getId();

        var msg = Message.builder()
                .conversationId(id)
                .senderId(adminId)
                .content(content)
                .type(type)
                .fileUrl(body.get("fileUrl"))
                .build();
        var saved = messageRepository.save(msg);

        var convo = conversationRepository.findById(id).orElse(null);
        if (convo != null) {
            String displayContent = content;
            if ("IMAGE".equals(type)) displayContent = "[Image]";
            else if ("LINK".equals(type)) displayContent = "[Link] " + content;
            convo.setLastMessage(displayContent);
            convo.setLastMessageAt(LocalDateTime.now());
            conversationRepository.save(convo);
        }

        var m = new LinkedHashMap<String, Object>();
        m.put("id", saved.getId().toString());
        m.put("conversationId", saved.getConversationId().toString());
        m.put("senderId", saved.getSenderId().toString());
        m.put("content", saved.getContent());
        m.put("type", saved.getType());
        m.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null);
        m.put("senderName", admins.get().getName());
        m.put("senderEmail", admins.get().getEmail());
        return ResponseEntity.ok(m);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable UUID id) {
        var msgs = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
        var enriched = new ArrayList<Map<String, Object>>();
        for (var msg : msgs) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", msg.getId().toString());
            m.put("conversationId", msg.getConversationId().toString());
            m.put("senderId", msg.getSenderId().toString());
            m.put("content", msg.getContent());
            m.put("type", msg.getType());
            m.put("fileUrl", msg.getFileUrl());
            m.put("readAt", msg.getReadAt() != null ? msg.getReadAt().toString() : null);
            m.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
            var user = userRepository.findById(msg.getSenderId()).orElse(null);
            if (user != null) {
                m.put("senderName", user.getName());
                m.put("senderEmail", user.getEmail());
                m.put("senderAvatar", user.getAvatarUrl());
            }
            enriched.add(m);
        }
        return ResponseEntity.ok(enriched);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChatImage(@RequestParam("file") MultipartFile file) {
        try {
            var result = storageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", result.getUrl(), "key", result.getKey()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed"));
        }
    }
}
