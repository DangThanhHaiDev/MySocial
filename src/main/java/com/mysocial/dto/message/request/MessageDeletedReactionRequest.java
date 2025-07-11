package com.mysocial.dto.message.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeletedReactionRequest {
    private Long messageReactionId;
    private Long receiverId;
    private Long messageId;
    private Long groupId;
}
