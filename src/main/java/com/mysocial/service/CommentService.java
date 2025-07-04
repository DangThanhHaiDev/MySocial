package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.Comment.CommentCreatedRequest;
import com.mysocial.model.Comment;
import com.mysocial.model.Post;
import com.mysocial.model.User;
import com.mysocial.repository.CommentRepository;
import com.mysocial.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    public ApiResponse<Comment> createCommentHandeler(CommentCreatedRequest request, User user, Long post_id){

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        comment.setUser(user);
        comment.setPost(postRepository.findById(post_id).orElseThrow(()->new RuntimeException("Post not found")));
        return new ApiResponse<>(201,"S",LocalDateTime.now(), commentRepository.save(comment));
    }
}
