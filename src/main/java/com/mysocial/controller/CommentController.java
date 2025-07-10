package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.dto.Comment.CommentTreeResponse;
import com.mysocial.model.Comment;
import com.mysocial.model.User;
import com.mysocial.service.CommentService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;
//    @PostMapping("/posts/{post_id}")
//    public ResponseEntity<ApiResponse<Comment>> createComment(@RequestHeader("Authorization") String jwt, @RequestBody CommentCreatedRequest request, @PathVariable Long post_id){
//        User user = userService.findUserProfileByJwt(jwt);
//        return new ResponseEntity<>(commentService.createCommentHandeler(request, user, post_id), HttpStatus.CREATED);
//    }

    @GetMapping("/posts/{post_id}")
    public ResponseEntity<java.util.List<CommentTreeResponse>> getCommentTree(@PathVariable Long post_id) {
        return ResponseEntity.ok(commentService.getCommentTreeDtoByPostId(post_id));
    }
}
