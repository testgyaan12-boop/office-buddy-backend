package com.officebuddy.community.controller;

import com.officebuddy.community.dto.ConversationDto;
import com.officebuddy.community.dto.MessageDto;
import com.officebuddy.community.service.CommunityService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class MessageController {

    private final CommunityService communityService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.getConversations(user.getId()));
    }

    @PostMapping("/conversations")
    public ResponseEntity<Map<String, String>> createConversation(Authentication auth, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        var otherId = UUID.fromString(body.get("otherUserId"));
        var conv = communityService.getOrCreateConversation(user.getId(), otherId);
        return ResponseEntity.ok(Map.of("id", conv.getId().toString()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(Authentication auth, @PathVariable UUID id) {
        return ResponseEntity.ok(communityService.getMessages(id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageDto> sendMessage(Authentication auth, @PathVariable UUID id, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        var msg = communityService.sendMessage(
                user.getId(), id,
                body.get("content"),
                body.get("type"),
                body.get("fileUrl")
        );
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<Void> markRead(Authentication auth, @PathVariable UUID id) {
        var user = (User) auth.getPrincipal();
        communityService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }
}
