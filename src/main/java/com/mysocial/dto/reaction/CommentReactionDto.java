package com.mysocial.dto.reaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionDto {
    private Long id;
    private Long userId;
    private Long reactionId;
    private String reactionType;
    private String reactionTitle;
    private String urlReaction;
} 