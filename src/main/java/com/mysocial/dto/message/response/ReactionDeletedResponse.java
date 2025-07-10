package com.mysocial.dto.message.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDeletedResponse {
    private Long messageId;
    private Long reactionId;
}
