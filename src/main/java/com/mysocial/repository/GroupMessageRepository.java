package com.mysocial.repository;

import com.mysocial.model.Group;
import com.mysocial.model.GroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupOrderByCreatedAtAsc(Group group);
} 