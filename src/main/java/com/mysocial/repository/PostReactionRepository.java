package com.mysocial.repository;

import com.mysocial.model.Post;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    void deleteByPostAndUser(Post post, User user);
}
