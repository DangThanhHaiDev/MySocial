package com.mysocial.ws;

import com.mysocial.model.Notification;
import com.mysocial.model.User;
import com.mysocial.service.NotificationService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationWebSocketController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Gửi notification realtime đến client
    public void sendNotificationToUser(Notification notification) {
        Long userId = notification.getUser().getId();
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    // (Tùy chọn) Nhận message từ client nếu cần
    @MessageMapping("/notifications/markAsRead")
    public void markAsRead(org.springframework.messaging.Message<?> message, @org.springframework.messaging.handler.annotation.Payload Long notificationId) {
        notificationService.markAsRead(notificationId);
    }
} 