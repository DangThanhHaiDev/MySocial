package com.mysocial.repository;

import com.mysocial.model.Comment;
import com.mysocial.model.CommentReaction;
import com.mysocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
    CommentReaction findByCommentAndUser(Comment comment, User user);
    void deleteByCommentAndUser(Comment comment, User user);
    long countByComment(Comment comment);
    List<CommentReaction> findByComment(Comment comment);
} 