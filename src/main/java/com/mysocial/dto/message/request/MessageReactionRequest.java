package com.mysocial.dto.message.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionRequest {
    private Long reactionId;
    private Long messageId;
    private Long receiverId;
    private Long groupId;
}

