package com.mysocial.dto.message.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummary {
    private Long id;
    private boolean isGroup;
    private String name;
    private String avatarUrl;

    private String lastMessage;
    private String lastSender;
    private String timestamp;

    private int unreadCount;
    private boolean isOnline;
    private boolean isDeleted;
}
