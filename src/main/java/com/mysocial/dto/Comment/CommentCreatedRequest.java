package com.mysocial.dto.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreatedRequest {
    private Long id;
    private String content;
    private Long parentId;
    private boolean hashtag  = false;
}
