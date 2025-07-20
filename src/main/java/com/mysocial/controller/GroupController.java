package com.mysocial.controller;

import com.mysocial.dto.group.AddMemberRequest;
import com.mysocial.dto.group.AddMembersRequest;
import com.mysocial.dto.group.CreateGroupRequest;
import com.mysocial.model.Group;
import com.mysocial.model.GroupMember;
import com.mysocial.model.User;
import com.mysocial.service.GroupService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;

    // Tạo nhóm mới
    @PostMapping("")
    public ResponseEntity<Group> createGroup(@RequestHeader("Authorization") String jwt, @RequestBody CreateGroupRequest request) {
        User creator = userService.findUserProfileByJwt(jwt);
        Group group = groupService.createGroup(request.getGroupName(), creator, request.getAvatarUrl(), request.getMemberIds());
        return ResponseEntity.ok(group);
    }


    // Lấy danh sách nhóm của user
    @GetMapping("")
    public ResponseEntity<List<Group>> getGroups(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        List<Group> groups = groupService.getGroupsOfUser(user);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getMember(@PathVariable Long groupId){
        return ResponseEntity.ok(groupService.getMemberByGroup(groupId));
    }

    // Thêm thành viên vào nhóm
    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupMember> addMember(@PathVariable Long groupId, @RequestBody AddMemberRequest request) {
        GroupMember member = groupService.addMember(groupId, request.getUserId());
        return ResponseEntity.ok(member);
    }
    @PostMapping("/{groupId}/members/add")
    public ResponseEntity<?> addMembers(@PathVariable Long groupId, @RequestBody AddMembersRequest request) {
        for (Long id: request.getUserIds()) {
            groupService.addMember(groupId, id);
        }
        return ResponseEntity.ok("Success!");
    }
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách thành viên nhóm
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupService.getMembers(groupId);
        return ResponseEntity.ok(members);
    }


} 