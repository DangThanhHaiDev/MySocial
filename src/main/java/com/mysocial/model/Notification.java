package com.mysocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Loại thông báo (ví dụ: FRIEND_REQUEST, MESSAGE, COMMENT, ...)
    @Column(nullable = false)
    private String type;

    // Nội dung thông báo
    @Column(nullable = false)
    private String message;

    // Id đối tượng liên quan (post, comment, v.v.)
    private Long relatedUserId;

    // Đã đọc hay chưa
    @Column(nullable = false)
    private Boolean isRead = false;

    // Thời gian tạo thông báo
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Id đối tượng liên quan (post, comment, v.v.)
    private Long referenceId;

    public Notification(User user, String type, String message, Long relatedUserId, Long referenceId) {
        this.user = user;
        this.type = type;
        this.message = message;
        this.relatedUserId = relatedUserId;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
} 