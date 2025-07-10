package com.mysocial.repository;

import com.mysocial.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    MessageReaction findByMessageAndUser(Message message, User user);
    List<MessageReaction> findByMessage(Message message);

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageReaction m where m.id = :id")
    void deleteMessageReaction(@Param("id") Long id);
}
