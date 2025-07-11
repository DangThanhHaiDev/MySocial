package com.mysocial.repository;

import com.mysocial.model.GroupMessageStatus;
import com.mysocial.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMessageStatusRepository extends JpaRepository<GroupMessageStatus, Long> {
    @Query("SELECT m FROM GroupMessageStatus m WHERE m.message.group.id = :groupId")
    List<GroupMessageStatus> findByGroupId(@Param("groupId") Long groupId);

    GroupMessageStatus findByMessage(Message message);
}

