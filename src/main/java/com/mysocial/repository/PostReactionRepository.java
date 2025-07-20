package com.mysocial.repository;

import com.mysocial.model.Post;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    void deleteByPostAndUser(Post post, User user);
    Optional<PostReaction> findByPostAndUser(Post post, User user);
    long countByPost(Post post);
    List<PostReaction> findByPost(Post post);
}
