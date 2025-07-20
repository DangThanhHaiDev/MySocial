package com.mysocial.dto.post;
import com.mysocial.model.Post;
import lombok.Data;
import java.util.List;
import com.mysocial.dto.Comment.CommentTreeResponse;

@Data
public class PostResponse {
    private Long id;
    private String image;
    private String video;
    private String content;
    private String location;
    private String createdAt;
    private boolean deleted;
    private UserInfo user;
    private List<CommentTreeResponse> comments;
    private List<String> images;

    private String currentUserReactionType;
    private int reactionCount;
    private boolean isAvatar;
    private Post.Privacy privacy;

    @Data
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String avatarUrl;
    }
} 