//package com.mysocial.ws;
//
//import com.mysocial.dto.ApiResponse;
//import com.mysocial.dto.Comment.CommentCreatedRequest;
//import com.mysocial.model.Comment;
//import com.mysocial.model.User;
//import com.mysocial.service.CommentService;
//import com.mysocial.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.messaging.Message;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@RestController
//public class CommentWebSocketController {
//    @Autowired
//    private CommentService commentService;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @MessageMapping("/comments/{postId}")
//    public void send(
//            CommentCreatedRequest request,
//            @DestinationVariable Long postId,
//            Message<?> message
//
//    ) {
//
//        Map<String, Object> sessionAttributes =
//                (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
//
//        String jwt = (String) sessionAttributes.get("token");
//        User user = userService.findUserProfileByJwt(jwt);
//        ApiResponse<Comment> response = commentService.createCommentHandeler(request, user, postId);
//
//        // Gửi đến topic động
//        messagingTemplate.convertAndSend("/topic/comments/" + postId, response);
//    }
//
//    @MessageMapping("/comments/delete/{commentId}")
//    public void deleteComment(@DestinationVariable Long commentId, Message<?> message) {
//        Map<String, Object> sessionAttributes = (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
//        String jwt = (String) sessionAttributes.get("token");
//        User user = userService.findUserProfileByJwt(jwt);
//        Comment comment = commentService.getCommentById(commentId);
//        // Chỉ cho phép chủ comment hoặc chủ post xóa
//        if (comment.getUser().getId().equals(user.getId()) || comment.getPost().getUser().getId().equals(user.getId())) {
//            commentService.deleteComment(commentId);
//            // Gửi thông báo xóa về topic của post
//            messagingTemplate.convertAndSend("/topic/comments/" + comment.getPost().getId(),
//                Map.of("action", "delete", "commentId", commentId));
//        }
//    }
//
//    @MessageMapping("/comments/update/{postId}")
//    public void updateComment(
//            CommentCreatedRequest request,
//            @DestinationVariable Long postId,
//            Message<?> message
//    ) {
//        Map<String, Object> sessionAttributes =
//                (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
//        String jwt = (String) sessionAttributes.get("token");
//        User user = userService.findUserProfileByJwt(jwt);
//        ApiResponse<Comment> response = commentService.updateCommentHandler(request, user);
//        // Gửi về topic của post để FE cập nhật
//        messagingTemplate.convertAndSend("/topic/comments/" + postId,
//            Map.of("action", "update", "commentId", request.getId(), "data", response.getData()));
//    }
//}

package com.mysocial.ws;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.model.Comment;
import com.mysocial.model.User;
import com.mysocial.service.CommentService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CommentWebSocketController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/comments/{postId}")
    public void send(
            CommentCreatedRequest request,
            @DestinationVariable Long postId,
            Message<?> message
    ) {
        Map<String, Object> sessionAttributes =
                (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");

        String jwt = (String) sessionAttributes.get("token");
        User user = userService.findUserProfileByJwt(jwt);
        ApiResponse<Comment> response = commentService.createCommentHandeler(request, user, postId);

        // Create enhanced response with action type
        Map<String, Object> wsResponse = new HashMap<>();
        wsResponse.put("action", "add");
        wsResponse.put("data", response.getData());

        // Add parentId if this is a reply
        if (request.getParentId() != null) {
            wsResponse.put("isReply", true);
            wsResponse.put("parentId", request.getParentId());
        } else {
            wsResponse.put("isReply", false);
        }

        // Send to topic
        messagingTemplate.convertAndSend("/topic/comments/" + postId, wsResponse);
    }

    @MessageMapping("/comments/delete/{commentId}")
    public void deleteComment(@DestinationVariable Long commentId, Message<?> message) {
        Map<String, Object> sessionAttributes = (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User user = userService.findUserProfileByJwt(jwt);
        Comment comment = commentService.getCommentById(commentId);

        // Check permissions
        if (comment.getUser().getId().equals(user.getId()) ||
                comment.getPost().getUser().getId().equals(user.getId())) {

            commentService.deleteComment(commentId);

            // Send deletion notification
            Map<String, Object> wsResponse = new HashMap<>();
            wsResponse.put("action", "delete");
            wsResponse.put("commentId", commentId);

            messagingTemplate.convertAndSend("/topic/comments/" + comment.getPost().getId(), wsResponse);
        }
    }

    @MessageMapping("/comments/update/{postId}")
    public void updateComment(
            CommentCreatedRequest request,
            @DestinationVariable Long postId,
            Message<?> message
    ) {
        Map<String, Object> sessionAttributes =
                (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User user = userService.findUserProfileByJwt(jwt);
        ApiResponse<Comment> response = commentService.updateCommentHandler(request, user);

        // Create enhanced update response
        Map<String, Object> wsResponse = new HashMap<>();
        wsResponse.put("action", "update");
        wsResponse.put("commentId", request.getId());
        wsResponse.put("data", response.getData());

        messagingTemplate.convertAndSend("/topic/comments/" + postId, wsResponse);
    }
}