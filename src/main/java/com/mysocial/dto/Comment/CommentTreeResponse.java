package com.mysocial.dto.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.mysocial.dto.reaction.CommentReactionDto;

@Data
public class CommentTreeResponse {
    private Long id;
    private String content;
    private String createdAt;
    private UserInfo user;
    private List<CommentTreeResponse> children;
    private boolean deleted;
    private boolean isHashtag;
    private List<CommentReactionDto> reactions;
    private CommentTreeResponse parent;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
    }
} 