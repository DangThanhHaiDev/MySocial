package com.mysocial.service;

import com.mysocial.dto.group.MemberResponse;
import com.mysocial.model.Group;
import com.mysocial.model.GroupMember;
import com.mysocial.model.User;
import com.mysocial.repository.GroupMemberRepository;
import com.mysocial.repository.GroupRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private UserRepository userRepository;

    public Group createGroup(String groupName, User creator, String avatarUrl, List<Long> memberIds) {
        Group group = new Group();
        group.setGroupName(groupName);
        group.setCreatedAt(LocalDateTime.now());
        group.setAvatarUrl(avatarUrl);
        group.setCreatedBy(creator);
        group = groupRepository.save(group);

        // Thêm creator vào nhóm với vai trò ADMIN
        GroupMember admin = new GroupMember();
        admin.setGroup(group);
        admin.setUser(creator);
        admin.setRole(GroupMember.Role.ADMIN);
        admin.setStatus(GroupMember.MemberStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        groupMemberRepository.save(admin);

        // Thêm các thành viên khác
        if (memberIds != null) {
            for (Long userId : memberIds) {
                if (userId.equals(creator.getId())) continue;
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    GroupMember member = new GroupMember();
                    member.setGroup(group);
                    member.setUser(user);
                    member.setRole(GroupMember.Role.MEMBER);
                    member.setStatus(GroupMember.MemberStatus.ACTIVE);
                    member.setCreatedAt(LocalDateTime.now());
                    groupMemberRepository.save(member);
                }
            }
        }
        return group;
    }

    public List<Group> getGroupsOfUser(User user) {
        List<GroupMember> memberships = groupMemberRepository.findByUserAndStatus(user, GroupMember.MemberStatus.ACTIVE);
        List<Group> groups = new ArrayList<>();
        for (GroupMember gm : memberships) {
            groups.add(gm.getGroup());
        }
        return groups;
    }

    public GroupMember addMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(GroupMember.Role.MEMBER);
        member.setStatus(GroupMember.MemberStatus.ACTIVE);
        member.setCreatedAt(LocalDateTime.now());
        return groupMemberRepository.save(member);
    }

    public void removeMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user);
        if (member != null) {
            member.setStatus(GroupMember.MemberStatus.LEFT);
            groupMemberRepository.save(member);
        }
    }

    public List<GroupMember> getMembers(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        return groupMemberRepository.findByGroupAndStatus(group, GroupMember.MemberStatus.ACTIVE);
    }

    public List<MemberResponse> getMemberByGroup(Long groupId){
        Group group = groupRepository.findById(groupId).orElseThrow();
        List<GroupMember> memberList = groupMemberRepository.findByGroupAndStatus(group, GroupMember.MemberStatus.ACTIVE);
        List<MemberResponse> response = new ArrayList<>();
        for (GroupMember member: memberList) {
            MemberResponse memberResponse = new MemberResponse();
            memberResponse.setId(member.getUser().getId());
            memberResponse.setRole(member.getRole()+"");
            memberResponse.setAvatar(member.getUser().getAvatarUrl());
            memberResponse.setFullName(member.getUser().getFirstName()+" "+member.getUser().getLastName());
            memberResponse.setJoinDate(member.getCreatedAt());
            response.add(memberResponse);
        }
        return response;
    }
} 