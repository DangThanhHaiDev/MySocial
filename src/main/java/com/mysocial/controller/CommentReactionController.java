package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.model.CommentReaction;
import com.mysocial.model.Reaction;
import com.mysocial.model.User;
import com.mysocial.service.CommentReactionService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment-reactions")
public class CommentReactionController {
    @Autowired
    private CommentReactionService commentReactionService;
    @Autowired
    private UserService userService;

    @PostMapping("/react")
    public ResponseEntity<ApiResponse<CommentReaction>> reactToComment(@RequestHeader("Authorization") String jwt, @RequestBody ReactionCreatedRequest request) {
        User user = userService.findUserProfileByJwt(jwt);
        ApiResponse<CommentReaction> response = commentReactionService.createEmotionComment(request, user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/react/{commentId}")
    public ResponseEntity<ApiResponse<String>> removeReaction(@RequestHeader("Authorization") String jwt, @PathVariable Long commentId) {
        User user = userService.findUserProfileByJwt(jwt);
        ApiResponse<String> response = commentReactionService.deleteEmotionComment(commentId, user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/count/{commentId}")
    public ResponseEntity<Integer> getReactionCount(@PathVariable Long commentId) {
        int count = commentReactionService.getReactionCountByCommentId(commentId);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/{commentId}")
    public ResponseEntity<?> getReactionByComment(@PathVariable Long commentId){
        return ResponseEntity.ok(commentReactionService.getReactionsByComment(commentId));
    }
} 