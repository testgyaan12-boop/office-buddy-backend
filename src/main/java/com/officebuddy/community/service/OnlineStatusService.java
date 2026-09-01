package com.officebuddy.community.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineStatusService {

    private final Map<UUID, Long> onlineUsers = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 60000;

    public void userConnected(UUID userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
    }

    public void userDisconnected(UUID userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(UUID userId) {
        var lastSeen = onlineUsers.get(userId);
        if (lastSeen == null) return false;
        if (System.currentTimeMillis() - lastSeen > TIMEOUT_MS) {
            onlineUsers.remove(userId);
            return false;
        }
        return true;
    }

    public Set<UUID> getOnlineUserIds() {
        var now = System.currentTimeMillis();
        onlineUsers.entrySet().removeIf(e -> now - e.getValue() > TIMEOUT_MS);
        return onlineUsers.keySet();
    }

    public void updateHeartbeat(UUID userId) {
        onlineUsers.put(userId, System.currentTimeMillis());
    }
}
