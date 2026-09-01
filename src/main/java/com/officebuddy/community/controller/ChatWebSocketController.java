package com.officebuddy.community.controller;

import com.officebuddy.community.dto.MessageDto;
import com.officebuddy.community.service.CommunityService;
import com.officebuddy.community.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CommunityService communityService;
    private final OnlineStatusService onlineStatusService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, String> payload, Principal principal) {
        var senderId = UUID.fromString(principal.getName());
        var conversationId = UUID.fromString(payload.get("conversationId"));
        var content = payload.get("content");
        var type = payload.get("type");
        var fileUrl = payload.get("fileUrl");

        var saved = communityService.sendMessage(senderId, conversationId, content, type, fileUrl);
        var destination = "/topic/conversations/" + conversationId;

        messagingTemplate.convertAndSend(destination, saved);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, String> payload, Principal principal) {
        var conversationId = payload.get("conversationId");
        var isTyping = payload.get("isTyping");
        var destination = "/topic/typing/" + conversationId;

        messagingTemplate.convertAndSend(destination, Map.of(
                "userId", principal.getName(),
                "isTyping", isTyping
        ));
    }

    @MessageMapping("/chat.online")
    public void online(Principal principal) {
        if (principal != null) {
            var userId = UUID.fromString(principal.getName());
            onlineStatusService.userConnected(userId);
            messagingTemplate.convertAndSend("/topic/online", Map.of(
                    "userId", userId.toString(),
                    "status", "ONLINE"
            ));
        }
    }

    @MessageMapping("/chat.offline")
    public void offline(Principal principal) {
        if (principal != null) {
            var userId = UUID.fromString(principal.getName());
            onlineStatusService.userDisconnected(userId);
            messagingTemplate.convertAndSend("/topic/online", Map.of(
                    "userId", userId.toString(),
                    "status", "OFFLINE"
            ));
        }
    }
}
