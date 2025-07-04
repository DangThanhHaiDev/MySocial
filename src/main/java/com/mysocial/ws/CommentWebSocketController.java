package com.mysocial.ws;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.model.Comment;
import com.mysocial.model.User;
import com.mysocial.service.CommentService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentWebSocketController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @MessageMapping("/comments/{postId}")
    @SendTo("/topic/comments/{postId}")
    public ApiResponse<Comment> send(
            CommentCreatedRequest request,
            @Header("Authorization") String jwt,
            @DestinationVariable Long postId
    ) {
        System.out.println(jwt);
        User user = userService.findUserProfileByJwt(jwt);
        return commentService.createCommentHandeler(request, user, postId);
    }

}
