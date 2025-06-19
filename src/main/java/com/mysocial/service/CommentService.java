package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.model.Comment;
import com.mysocial.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public ApiResponse<Comment> createCommentHandeler(CommentCreatedRequest request){

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        return new ApiResponse<>(201,"S",LocalDateTime.now(), commentRepository.save(comment));
    }
}
