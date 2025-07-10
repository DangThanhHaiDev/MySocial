package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.reaction.ReactionCreatedRequest;
import com.mysocial.dto.reaction.CommentReactionDto;
import com.mysocial.model.Comment;
import com.mysocial.model.CommentReaction;
import com.mysocial.model.User;
import com.mysocial.model.Reaction;
import com.mysocial.repository.CommentReactionRepository;
import com.mysocial.repository.CommentRepository;
import com.mysocial.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentReactionService {
    @Autowired
    private CommentReactionRepository commentReactionRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public ApiResponse<CommentReaction> createEmotionComment(ReactionCreatedRequest request, User user){
        Comment comment = commentRepository.findById(request.getCommentId()).orElseThrow(() -> new RuntimeException("Comment not found"));
        CommentReaction existingReaction = commentReactionRepository.findByCommentAndUser(comment, user);
        if (existingReaction != null) {
            existingReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow(() -> new RuntimeException("Reaction not found")));
            existingReaction.setCreatedAt(LocalDateTime.now());
            CommentReaction savedCommentReaction = commentReactionRepository.save(existingReaction);
            return new ApiResponse<>(200, "Updated", LocalDateTime.now(), savedCommentReaction);
        } else {
            CommentReaction commentReaction = new CommentReaction();
            commentReaction.setCreatedAt(LocalDateTime.now());
            commentReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow(() -> new RuntimeException("Reaction not found")));
            commentReaction.setComment(comment);
            commentReaction.setUser(user);
            CommentReaction savedCommentReaction = commentReactionRepository.save(commentReaction);
            return new ApiResponse<>(201, "Created", LocalDateTime.now(), savedCommentReaction);
        }
    }

    @Transactional
    public ApiResponse<String> deleteEmotionComment(Long commentId, User user){
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        commentReactionRepository.deleteByCommentAndUser(comment, user);
        return new ApiResponse<>(200, "Success", LocalDateTime.now(), null);
    }

    public int getReactionCountByCommentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        return (int) commentReactionRepository.countByComment(comment);
    }

    public List<CommentReactionDto> getReactionsByComment(Comment comment) {
        List<CommentReaction> reactions = commentReactionRepository.findByComment(comment);
        return reactions.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<CommentReaction> getReactionsByComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        List<CommentReaction> reactions = commentReactionRepository.findByComment(comment);
        return reactions;
    }

    public CommentReactionDto toDto(CommentReaction reaction) {
        Reaction r = reaction.getReaction();
        return new CommentReactionDto(
            reaction.getId(),
            reaction.getUser() != null ? reaction.getUser().getId() : null,
            r != null ? r.getId() : null,
            r != null ? r.getReactionType() : null,
            r != null ? r.getTitle() : null,
            r != null ? r.getUrlReaction() : null
        );
    }
} 