package com.mysocial.service;

import com.mysocial.dto.PagedResponse;
import com.mysocial.model.Notification;
import com.mysocial.model.User;
import com.mysocial.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification) {
        System.out.println("Tạo thong báo nè");
        return notificationRepository.save(notification);
    }

    public PagedResponse<Notification> getNotificationsByUser(User user, int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return new PagedResponse<>(notificationPage);
    }

    public List<Notification> getUnreadNotificationsByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        notificationOpt.ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
} 