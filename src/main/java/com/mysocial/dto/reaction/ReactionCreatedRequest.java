package com.mysocial.dto.reaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionCreatedRequest {
    private Long reactionId;
    private Long postId;
    private Long commentId;
}
