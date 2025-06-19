package com.mysocial.repository;

import com.mysocial.model.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

}
