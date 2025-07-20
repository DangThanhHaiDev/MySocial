package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.model.User;
import com.mysocial.service.PostReactionService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post/reaction")
public class PostReactionController {
    @Autowired
    private UserService userService;
    @Autowired
    private PostReactionService postReactionService;
    @PostMapping
    public ResponseEntity<?> createEmotionPost(@RequestHeader("Authorization") String jwt, @RequestBody ReactionCreatedRequest request){
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(postReactionService.createEmotionPost(request, user), HttpStatus.CREATED);
    }
    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deleteEmotionPost(@RequestHeader("Authorization") String jwt ,@PathVariable Long post_id){
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(postReactionService.deleteEmotionPost(post_id, user), HttpStatus.OK);
    }
    @GetMapping("/{postId}")
    public ResponseEntity<?> getReactionByPost(@PathVariable Long postId){
        return ResponseEntity.ok(postReactionService.getReactionByPost(postId));
    }
}
