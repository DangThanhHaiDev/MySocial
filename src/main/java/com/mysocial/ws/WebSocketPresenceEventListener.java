package com.mysocial.ws;

import com.mysocial.model.User;
import com.mysocial.service.PresenceService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Component
public class WebSocketPresenceEventListener {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private UserService userService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        String jwt = extractJwt(event);
        if (jwt != null) {
            User user = userService.findUserProfileByJwt(jwt);
            String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
            presenceService.userConnected(user.getId(), sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long userId = presenceService.findUserIdBySessionId(sessionId);
        if (userId != null) {
            presenceService.userDisconnected(userId, sessionId);
        }
    }

    private String extractJwt(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        List<String> auth = accessor.getNativeHeader("Authorization");
        if (auth != null && !auth.isEmpty()) {
            return auth.get(0).replace("Bearer ", "");
        }
        return null;
    }
}
