package com.officebuddy.community.controller;

import com.officebuddy.community.service.OnlineStatusService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class OnlineStatusController {

    private final OnlineStatusService onlineStatusService;

    @GetMapping("/online")
    public ResponseEntity<List<String>> getOnlineUsers() {
        var ids = onlineStatusService.getOnlineUserIds().stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(Authentication auth) {
        var user = (User) auth.getPrincipal();
        onlineStatusService.updateHeartbeat(user.getId());
        return ResponseEntity.ok().build();
    }
}
