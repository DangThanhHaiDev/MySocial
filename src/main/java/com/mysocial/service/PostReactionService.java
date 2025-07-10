package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.model.Notification;
import com.mysocial.model.Post;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import com.mysocial.repository.PostReactionRepository;
import com.mysocial.repository.PostRepository;
import com.mysocial.repository.ReactionRepository;
import com.mysocial.ws.NotificationWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostReactionService {
    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationWebSocketController notificationWebSocketController;
    @Transactional
    public ApiResponse<PostReaction> createEmotionPost(ReactionCreatedRequest request, User user){
        Post post = postRepository.findById(request.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));
        
        // Kiểm tra xem user đã có reaction cho post này chưa
        PostReaction existingReaction = postReactionRepository.findByPostAndUser(post, user).orElse(null);
        if (existingReaction != null) {
            // Nếu đã có reaction, update reaction mới
            existingReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow(() -> new RuntimeException("Reaction not found")));
            existingReaction.setCreatedAt(LocalDateTime.now());
            PostReaction savedPostReaction = postReactionRepository.save(existingReaction);

            if(!post.getUser().getId().equals(user.getId())){
                Notification notification = new Notification();
                notification.setUser(post.getUser());
                notification.setType("REACTION");
                notification.setMessage(user.getFirstName() + " " + user.getLastName() + " đã thả cảm xúc bài viết của bạn!");
                notification.setRelatedUserId(post.getId());
                notification.setIsRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                notification.setReferenceId(user.getId());
                notificationService.createNotification(notification);
                notificationWebSocketController.sendNotificationToUser(notification);
            }

            return new ApiResponse<>(200, "Updated", LocalDateTime.now(), savedPostReaction);
        } else {
            // Tạo reaction mới
            PostReaction postReaction = new PostReaction();
            postReaction.setCreatedAt(LocalDateTime.now());
            postReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow(() -> new RuntimeException("Reaction not found")));
            postReaction.setPost(post);
            postReaction.setUser(user);


            if(!post.getUser().getId().equals(user.getId())){
                Notification notification = new Notification();
                notification.setUser(post.getUser());
                notification.setType("REACTION");
                notification.setMessage(user.getFirstName() + " " + user.getLastName() + " đã thả cảm xúc bài viết của bạn!");
                notification.setRelatedUserId(post.getId());
                notification.setIsRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                notification.setReferenceId(user.getId());
                notificationService.createNotification(notification);
                notificationWebSocketController.sendNotificationToUser(notification);
            }
            PostReaction savedPostReaction = postReactionRepository.save(postReaction);

            return new ApiResponse<>(201, "Created", LocalDateTime.now(), savedPostReaction);
        }
    }

    @Transactional
    public ApiResponse<String> deleteEmotionPost(Long postId, User user){
        Post post = postRepository.findById(postId).orElseThrow(()-> new RuntimeException("Post not found"));
        postReactionRepository.deleteByPostAndUser(post, user);
        return new ApiResponse<>(200, "Success", LocalDateTime.now(), null );
    }

    public int getReactionCountByPostId(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return (int) postReactionRepository.countByPost(post);
    }
}
