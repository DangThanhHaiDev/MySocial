package com.mysocial.service;

import com.mysocial.model.Group;
import com.mysocial.model.GroupMessage;
import com.mysocial.model.User;
import com.mysocial.repository.GroupMessageRepository;
import com.mysocial.repository.GroupRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMessageService {
    @Autowired
    private GroupMessageRepository groupMessageRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    public GroupMessage sendMessage(Long groupId, Long senderId, String content, String type) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();
        GroupMessage message = new GroupMessage();
        message.setGroup(group);
        message.setSender(sender);
        message.setContent(content);
        message.setType(type);
        message.setCreatedAt(LocalDateTime.now());
        return groupMessageRepository.save(message);
    }

    public List<GroupMessage> getMessages(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        return groupMessageRepository.findByGroupOrderByCreatedAtAsc(group);
    }
} 