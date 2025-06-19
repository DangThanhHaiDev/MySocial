package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.model.Comment;
import com.mysocial.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    public ResponseEntity<ApiResponse<Comment>> createComment(@RequestBody CommentCreatedRequest request){
        return new ResponseEntity<>(commentService.createCommentHandeler(request), HttpStatus.CREATED);
    }
}
