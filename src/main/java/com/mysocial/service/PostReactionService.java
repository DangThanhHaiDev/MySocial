package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.model.Post;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import com.mysocial.repository.PostReactionRepository;
import com.mysocial.repository.PostRepository;
import com.mysocial.repository.ReactionRepository;
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
    public ApiResponse<PostReaction> createEmotionPost(ReactionCreatedRequest request, User user){
        PostReaction postReaction = new PostReaction();
        postReaction.setCreatedAt(LocalDateTime.now());

        postReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow(() -> new RuntimeException("Reaction not found")));
        postReaction.setPost(postRepository.findById(request.getPostId()).orElseThrow(() -> new RuntimeException("Post not found")));
        postReaction.setUser(user);

        PostReaction savedPostReaction = postReactionRepository.save(postReaction);

        return new ApiResponse<>(201, "Success", LocalDateTime.now(), savedPostReaction);
    }

    @Transactional
    public ApiResponse<String> deleteEmotionPost(Long postId, User user){
        Post post = postRepository.findById(postId).orElseThrow(()-> new RuntimeException("Post not found"));
        postReactionRepository.deleteByPostAndUser(post, user);
        return new ApiResponse<>(200, "Success", LocalDateTime.now(), null );
    }
}
