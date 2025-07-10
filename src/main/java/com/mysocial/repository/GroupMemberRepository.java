package com.mysocial.repository;

import com.mysocial.model.Group;
import com.mysocial.model.GroupMember;
import com.mysocial.model.User;
import com.mysocial.model.GroupMember.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByUserAndStatus(User user, MemberStatus status);
    List<GroupMember> findByGroupAndStatus(Group group, MemberStatus status);
    GroupMember findByGroupAndUser(Group group, User user);
} 