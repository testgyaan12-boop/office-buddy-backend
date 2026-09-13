package com.officebuddy.admin;

import com.officebuddy.community.entity.ChatGroup;
import com.officebuddy.community.entity.GroupMember;
import com.officebuddy.community.entity.GroupMessage;
import com.officebuddy.community.repository.ChatGroupRepository;
import com.officebuddy.community.repository.GroupMemberRepository;
import com.officebuddy.community.repository.GroupMessageRepository;
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
@RequestMapping("/api/v1/admin/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroupController {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<?> listGroups() {
        var all = chatGroupRepository.findAll();
        var result = new ArrayList<Map<String, Object>>();
        var admins = userRepository.findByEmail("admin@officebuddy.app");
        UUID adminId = admins.map(a -> a.getId()).orElse(null);

        for (var g : all) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", g.getId().toString());
            m.put("name", g.getName());
            m.put("description", g.getDescription());
            m.put("groupAvatar", g.getGroupAvatar());
            m.put("adminId", g.getAdminId().toString());
            m.put("createdAt", g.getCreatedAt() != null ? g.getCreatedAt().toString() : null);
            m.put("isActive", g.getIsActive());

            long pending = groupMemberRepository.countByGroupIdAndStatus(g.getId(), "PENDING");
            long approved = groupMemberRepository.countByGroupIdAndStatus(g.getId(), "APPROVED");
            long rejected = groupMemberRepository.countByGroupIdAndStatus(g.getId(), "REJECTED");
            m.put("pendingCount", pending);
            m.put("memberCount", approved);
            m.put("rejectedCount", rejected);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String desc = body.get("description");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name required"));
        }
        var admins = userRepository.findByEmail("admin@officebuddy.app");
        UUID adminId = admins.map(a -> a.getId()).orElse(null);

        var group = ChatGroup.builder()
                .name(name)
                .description(desc)
                .adminId(adminId)
                .groupAvatar(body.get("groupAvatar"))
                .build();
        var saved = chatGroupRepository.save(group);

        var m = new LinkedHashMap<String, Object>();
        m.put("id", saved.getId().toString());
        m.put("name", saved.getName());
        m.put("description", saved.getDescription());
        m.put("adminId", saved.getAdminId().toString());
        m.put("memberCount", 0);
        m.put("pendingCount", 0);
        m.put("rejectedCount", 0);
        return ResponseEntity.ok(m);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        var opt = chatGroupRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var g = opt.get();
        if (body.containsKey("name")) g.setName(body.get("name"));
        if (body.containsKey("description")) g.setDescription(body.get("description"));
        if (body.containsKey("groupAvatar")) g.setGroupAvatar(body.get("groupAvatar"));
        chatGroupRepository.save(g);
        return ResponseEntity.ok(Map.of("id", g.getId().toString(), "name", g.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID id) {
        if (!chatGroupRepository.existsById(id)) return ResponseEntity.notFound().build();
        groupMessageRepository.deleteAll(groupMessageRepository.findByGroupIdOrderByCreatedAtAsc(id));
        groupMemberRepository.findByGroupIdOrderByCreatedAtDesc(id).forEach(groupMemberRepository::delete);
        chatGroupRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> listMembers(@PathVariable UUID id) {
        var members = groupMemberRepository.findByGroupIdOrderByCreatedAtDesc(id);
        var result = new ArrayList<Map<String, Object>>();
        for (var mem : members) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", mem.getId().toString());
            m.put("groupId", mem.getGroupId().toString());
            m.put("userId", mem.getUserId().toString());
            m.put("status", mem.getStatus());
            m.put("createdAt", mem.getCreatedAt() != null ? mem.getCreatedAt().toString() : null);
            var user = userRepository.findById(mem.getUserId());
            if (user.isPresent()) {
                m.put("userName", user.get().getName());
                m.put("userEmail", user.get().getEmail());
            } else {
                m.put("userName", "Unknown");
                m.put("userEmail", "");
            }
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> updateMemberStatus(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status must be APPROVED or REJECTED"));
        }
        var opt = groupMemberRepository.findById(memberId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var m = opt.get();
        m.setStatus(status);
        groupMemberRepository.save(m);
        return ResponseEntity.ok(Map.of("status", status));
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable UUID groupId, @PathVariable UUID memberId) {
        if (!groupMemberRepository.existsById(memberId)) return ResponseEntity.notFound().build();
        groupMemberRepository.deleteById(memberId);
        return ResponseEntity.ok(Map.of("removed", true));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<?> messages(@PathVariable UUID id) {
        var msgs = groupMessageRepository.findByGroupIdOrderByCreatedAtAsc(id);
        var result = new ArrayList<Map<String, Object>>();
        for (var msg : msgs) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", msg.getId().toString());
            m.put("groupId", msg.getGroupId().toString());
            m.put("senderId", msg.getSenderId().toString());
            m.put("content", msg.getContent());
            m.put("type", msg.getType());
            m.put("fileUrl", msg.getFileUrl());
            m.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
            var user = userRepository.findById(msg.getSenderId());
            if (user.isPresent()) {
                m.put("senderName", user.get().getName());
                m.put("senderEmail", user.get().getEmail());
            } else {
                m.put("senderName", "Unknown");
                m.put("senderEmail", "");
            }
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        if (!"1234".equals(adminKey)) {
            return ResponseEntity.status(403).body(Map.of("message", "Invalid admin key"));
        }
        String content = body.getOrDefault("content", "");
        String type = body.getOrDefault("type", "TEXT");
        String fileUrl = body.get("fileUrl");
        if ((content == null || content.isBlank()) && ("TEXT".equals(type))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content required"));
        }
        var admins = userRepository.findByEmail("admin@officebuddy.app");
        if (admins.isEmpty()) return ResponseEntity.status(500).body(Map.of("message", "Admin not found"));
        UUID adminId = admins.get().getId();

        var msg = GroupMessage.builder()
                .groupId(id)
                .senderId(adminId)
                .content(content)
                .type(type)
                .fileUrl(fileUrl)
                .build();
        var saved = groupMessageRepository.save(msg);

        String displayContent = content;
        if ("IMAGE".equals(type)) displayContent = "[Image]";
        else if ("LINK".equals(type)) displayContent = "[Link] " + content;

        var m = new LinkedHashMap<String, Object>();
        m.put("id", saved.getId().toString());
        m.put("groupId", saved.getGroupId().toString());
        m.put("senderId", saved.getSenderId().toString());
        m.put("content", saved.getContent());
        m.put("type", saved.getType());
        m.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null);
        m.put("senderName", admins.get().getName());
        m.put("senderEmail", admins.get().getEmail());
        return ResponseEntity.ok(m);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadGroupImage(@RequestParam("file") MultipartFile file) {
        try {
            var result = storageService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", result.getUrl(), "key", result.getKey()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed"));
        }
    }
}
