package com.mysocial.ws;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.model.Notification;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import com.mysocial.service.PostReactionService;
import com.mysocial.service.ReactionService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReactionWebSocketController {
    @Autowired
    private PostReactionService postReactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/reactions/{postId}")
    public void handleReaction(
            Map<String, Object> request,
            @DestinationVariable Long postId,
            Message<?> message
    ) {
        Map<String, Object> sessionAttributes =
                (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");

        String jwt = (String) sessionAttributes.get("token");
        User user = userService.findUserProfileByJwt(jwt);
        
        String action = (String) request.get("action");
        String reactionType = (String) request.get("reactionType");

        if ("add".equals(action)) {
            // Thêm reaction
            ReactionCreatedRequest reactionRequest = new ReactionCreatedRequest();
            reactionRequest.setPostId(postId);
            reactionRequest.setReactionId(reactionService.getReactionByType(reactionType).getId());
            
            ApiResponse<PostReaction> response = postReactionService.createEmotionPost(reactionRequest, user);
            int reactionCount = postReactionService.getReactionCountByPostId(postId);
            // Gửi thông báo về topic
            messagingTemplate.convertAndSend("/topic/reactions/" + postId, 
                Map.of("action", "add", "data", Map.of(
                    "reaction", response.getData().getReaction(),
                    "reactionCount", reactionCount,
                    "userId", user.getId()
                )));

            //Gửi thông báo đến chủ bài viết

                
        } else if ("remove".equals(action)) {
            // Xóa reaction
            postReactionService.deleteEmotionPost(postId, user);
            int reactionCount = postReactionService.getReactionCountByPostId(postId);
            // Gửi thông báo về topic
            messagingTemplate.convertAndSend("/topic/reactions/" + postId, 
                Map.of("action", "remove", "userId", user.getId(), "reactionCount", reactionCount));
        }
    }
} 