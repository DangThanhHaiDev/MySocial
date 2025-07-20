package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.dto.Comment.CommentTreeResponse;
import com.mysocial.model.*;
import com.mysocial.repository.CommentRepository;
import com.mysocial.repository.PostRepository;
import com.mysocial.ws.NotificationWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mysocial.service.CommentReactionService;
import com.mysocial.dto.reaction.CommentReactionDto;
import com.mysocial.util.BadWordFilter;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentReactionService commentReactionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationWebSocketController notificationWebSocketController;

    public ApiResponse<Comment> createCommentHandeler(CommentCreatedRequest request, User user, Long post_id){
        if (BadWordFilter.containsBadWords(request.getContent())) {
            throw new RuntimeException("Bình luận chứa từ ngữ không phù hợp!");
        }
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        comment.setUser(user);
        comment.setPost(postRepository.findById(post_id).orElseThrow(() -> new RuntimeException("Post not found")));
        comment.setHashtag(request.isHashtag());
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId()).orElse(null);
            comment.setParent(parent);
        }

        Post post = postRepository.findById(post_id).orElseThrow();
        if(!post.getUser().getId().equals(user.getId())){
            Notification notification = new Notification();
            notification.setUser(post.getUser());
            notification.setType("COMMENT");
            notification.setMessage(user.getFirstName() + " " + user.getLastName() + " đã thả bình luận bài viết của bạn!");
            notification.setRelatedUserId(post.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(java.time.LocalDateTime.now());
            notification.setReferenceId(user.getId());
            notificationService.createNotification(notification);
            notificationWebSocketController.sendNotificationToUser(notification);
        }

        return new ApiResponse<>(201,"S",LocalDateTime.now(), commentRepository.save(comment));
    }

    public java.util.List<Comment> getCommentTreeByPostId(Long postId) {
        java.util.List<Comment> roots = commentRepository.findByPostIdAndParentIsNullAndIsDeletedFalse(postId);
        for (Comment root : roots) {
            loadChildren(root);
        }
        return roots;
    }

    private void loadChildren(Comment comment) {
        java.util.List<Comment> children = commentRepository.findByParentIdAndIsDeletedFalse(comment.getId());
        comment.setChildren(children);
        for (Comment child : children) {
            loadChildren(child);
        }
    }

    public java.util.List<CommentTreeResponse> getCommentTreeDtoByPostId(Long postId) {
        java.util.List<Comment> roots = commentRepository.findByPostIdAndParentIsNullAndIsDeletedFalse(postId);
        java.util.List<CommentTreeResponse> dtos = new java.util.ArrayList<>();
        for (Comment root : roots) {
            dtos.add(toDto(root));
        }
        return dtos;
    }


    public CommentTreeResponse toDto(Comment comment) {
        CommentTreeResponse dto = new CommentTreeResponse();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null);
        dto.setDeleted(comment.isDeleted());
        dto.setHashtag(comment.isHashtag());
        // User info
        CommentTreeResponse.UserInfo userInfo = new CommentTreeResponse.UserInfo();
        if (comment.getUser() != null) {
            userInfo.setId(comment.getUser().getId());
            userInfo.setFirstName(comment.getUser().getFirstName());
            userInfo.setLastName(comment.getUser().getLastName());
            userInfo.setAvatarUrl(comment.getUser().getAvatarUrl());
        }
        dto.setUser(userInfo);
        // Children
        java.util.List<CommentTreeResponse> childDtos = new java.util.ArrayList<>();
        if (comment.getChildren() != null) {
            for (Comment child : comment.getChildren()) {
                childDtos.add(toDto(child));
            }
        }
        dto.setChildren(childDtos);
        // Reactions
        java.util.List<CommentReactionDto> reactions = commentReactionService.getReactionsByComment(comment);
        dto.setReactions(reactions);
        return dto;
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public void deleteComment(Long id) {
        Comment comment = getCommentById(id);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public ApiResponse<Comment> updateCommentHandler(CommentCreatedRequest request, User user) {
        if (request.getId() == null) throw new RuntimeException("Comment id is required");
        Comment comment = getCommentById(request.getId());
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        if (comment.isDeleted()) throw new RuntimeException("Comment đã bị xóa");
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        return new ApiResponse<>(200, "Cập nhật thành công", LocalDateTime.now(), commentRepository.save(comment));
    }
}
