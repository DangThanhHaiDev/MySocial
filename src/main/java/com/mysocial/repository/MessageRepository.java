package com.mysocial.repository;

import com.mysocial.model.Message;
import com.mysocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.group.id = :groupId")
    List<Message> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT m FROM Message m WHERE ((m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1)) AND m.group IS NULL")
    List<Message> findMessagesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, org.springframework.data.domain.Sort sort);


}
