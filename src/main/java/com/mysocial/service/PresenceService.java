package com.mysocial.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private final Map<Long, Set<String>> onlineUsers = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionIdToUserId = new ConcurrentHashMap<>();


    public void userConnected(Long userId, String sessionId) {
        onlineUsers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void userDisconnected(Long userId, String sessionId) {
        Set<String> sessions = onlineUsers.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
            }
        }
    }

    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
    public Set<Long> getAllOnlineUserIds() {
        return onlineUsers.keySet();
    }
    public Long findUserIdBySessionId(String sessionId) {
        return sessionIdToUserId.get(sessionId);
    }
}
