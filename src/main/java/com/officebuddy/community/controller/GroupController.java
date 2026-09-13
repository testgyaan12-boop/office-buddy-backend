package com.officebuddy.community.controller;

import com.officebuddy.community.entity.ChatGroup;
import com.officebuddy.community.entity.GroupMember;
import com.officebuddy.community.entity.GroupMessage;
import com.officebuddy.community.repository.ChatGroupRepository;
import com.officebuddy.community.repository.GroupMemberRepository;
import com.officebuddy.community.repository.GroupMessageRepository;
import com.officebuddy.user.User;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final UserRepository userRepository;

    private UUID userId(Authentication auth) { return ((User) auth.getPrincipal()).getId(); }

    @GetMapping
    public ResponseEntity<?> listGroups() {
        var all = chatGroupRepository.findAll();
        var result = new ArrayList<Map<String, Object>>();
        for (var g : all) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", g.getId().toString());
            m.put("name", g.getName());
            m.put("description", g.getDescription());
            m.put("groupAvatar", g.getGroupAvatar());
            long approved = groupMemberRepository.countByGroupIdAndStatus(g.getId(), "APPROVED");
            m.put("memberCount", approved);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<?> myGroups(Authentication auth) {
        UUID uid = userId(auth);
        var groups = chatGroupRepository.findGroupsByMemberUserId(uid);
        var result = new ArrayList<Map<String, Object>>();
        for (var g : groups) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", g.getId().toString());
            m.put("name", g.getName());
            m.put("description", g.getDescription());
            m.put("groupAvatar", g.getGroupAvatar());
            long approved = groupMemberRepository.countByGroupIdAndStatus(g.getId(), "APPROVED");
            m.put("memberCount", approved);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my/requests")
    public ResponseEntity<?> myRequests(Authentication auth) {
        UUID uid = userId(auth);
        var members = groupMemberRepository.findByUserIdAndStatusOrderByCreatedAtDesc(uid, "PENDING");
        var result = new ArrayList<Map<String, Object>>();
        for (var mem : members) {
            var m = new LinkedHashMap<String, Object>();
            m.put("id", mem.getId().toString());
            m.put("groupId", mem.getGroupId().toString());
            m.put("status", mem.getStatus());
            var group = chatGroupRepository.findById(mem.getGroupId());
            m.put("name", group.map(ChatGroup::getName).orElse("Unknown"));
            m.put("description", group.map(g -> g.getDescription()).orElse(null));
            long approved = groupMemberRepository.countByGroupIdAndStatus(mem.getGroupId(), "APPROVED");
            m.put("memberCount", approved);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{groupId}/request")
    public ResponseEntity<?> requestJoin(@PathVariable UUID groupId, Authentication auth) {
        UUID uid = userId(auth);
        if (!chatGroupRepository.existsById(groupId)) {
            return ResponseEntity.notFound().build();
        }
        var existing = groupMemberRepository.findByGroupIdAndUserId(groupId, uid);
        if (existing.isPresent()) {
            var m = existing.get();
            if ("PENDING".equals(m.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Request already pending"));
            }
            if ("APPROVED".equals(m.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Already a member"));
            }
            m.setStatus("PENDING");
            groupMemberRepository.save(m);
            return ResponseEntity.ok(Map.of("message", "Request re-submitted"));
        }
        var member = GroupMember.builder()
                .groupId(groupId)
                .userId(uid)
                .status("PENDING")
                .build();
        groupMemberRepository.save(member);
        return ResponseEntity.ok(Map.of("message", "Request sent"));
    }

    @GetMapping("/{groupId}/is-member")
    public ResponseEntity<?> isMember(@PathVariable UUID groupId, Authentication auth) {
        UUID uid = userId(auth);
        var opt = groupMemberRepository.findByGroupIdAndUserId(groupId, uid);
        if (opt.isEmpty()) return ResponseEntity.ok(Map.of("status", "NONE"));
        return ResponseEntity.ok(Map.of("status", opt.get().getStatus()));
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<?> messages(@PathVariable UUID groupId, Authentication auth) {
        UUID uid = userId(auth);
        var isMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(groupId, uid, "APPROVED");
        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("message", "Not a member"));
        }
        var msgs = groupMessageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
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
            m.put("senderName", user.map(u -> u.getName()).orElse("Unknown"));
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID groupId,
            Authentication auth,
            @RequestBody Map<String, String> body) {
        UUID uid = userId(auth);
        var isMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(groupId, uid, "APPROVED");
        if (!isMember) {
            return ResponseEntity.status(403).body(Map.of("message", "Not a member"));
        }
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Content required"));
        }
        var msg = GroupMessage.builder()
                .groupId(groupId)
                .senderId(uid)
                .content(content)
                .type(body.getOrDefault("type", "TEXT"))
                .build();
        var saved = groupMessageRepository.save(msg);
        var user = userRepository.findById(uid);
        var m = new LinkedHashMap<String, Object>();
        m.put("id", saved.getId().toString());
        m.put("groupId", saved.getGroupId().toString());
        m.put("senderId", saved.getSenderId().toString());
        m.put("content", saved.getContent());
        m.put("type", saved.getType());
        m.put("createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null);
        m.put("senderName", user.map(u -> u.getName()).orElse("Unknown"));
        return ResponseEntity.ok(m);
    }
}
