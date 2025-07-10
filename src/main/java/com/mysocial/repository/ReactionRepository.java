package com.mysocial.repository;

import com.mysocial.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByReactionType(String reactionType);
}
