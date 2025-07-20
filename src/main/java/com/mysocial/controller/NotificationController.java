package com.mysocial.controller;

import com.mysocial.model.Notification;
import com.mysocial.model.User;
import com.mysocial.service.NotificationService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    // Lấy danh sách notification của user
    @GetMapping("")
    public ResponseEntity<?> getNotifications(@RequestHeader("Authorization") String jwt,
                                                               @RequestParam("size") int size,
                                                               @RequestParam("page") int page) {
        User user = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(notificationService.getNotificationsByUser(user, size, page));
    }

    // Lấy danh sách notification chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(notificationService.getUnreadNotificationsByUser(user));
    }

    // Đánh dấu đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // Xóa notification
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
} 