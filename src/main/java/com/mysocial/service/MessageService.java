package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.message.request.MessageRequest;
import com.mysocial.dto.message.response.ConversationSummary;
import com.mysocial.model.*;
import com.mysocial.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private PresenceService presenceService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private GroupMessageStatusRepository groupMessageStatusRepository;

    public ApiResponse<Message> saveMessage(MessageRequest request, User sender, String fileUrl){
        User receiver = null;
        Group group = null;
        Message message = new Message();
        GroupMessageStatus groupMessageStatus = new GroupMessageStatus();

        if(request.getReceiverId() != null){
            receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new RuntimeException("User not found"));
            message.setReceiver(receiver);
        }
        else{
            group = groupRepository.findById(request.getGroupId()).orElseThrow(() -> new RuntimeException("Group not found"));
            message.setGroup(group);
            groupMessageStatus.setMessage(message);
            groupMessageStatus.getUserIds().add(sender.getId());
        }
        message.setMessageType(request.getType());
        message.setDeleted(false);
        message.setContent(request.getContent());
        message.setEdited(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.SENT);
        message.setSender(sender);
        if(request.getType() == Message.MessageType.IMAGE){
            message.setImageUrl(fileUrl);
        }

        if(request.getReplyToId()!=null){
            message.setReplyTo(messageRepository.findById(request.getReplyToId()).orElseThrow(() -> new RuntimeException("Message not found")));
        }

        Message response = messageRepository.save(message);
        if(request.getGroupId()!=null){
            groupMessageStatusRepository.save(groupMessageStatus);
        }

        return new ApiResponse<>(201, "Success", LocalDateTime.now(),response);
    }

    public List<ConversationSummary> getConversationsForUser(Long userId) {
        List<ConversationSummary> result = new ArrayList<>();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return result;
        User user = userOpt.get();

        // 1. Cá nhân: lấy tất cả user từng nhắn với userId
        List<Message> personalMessages = messageRepository.findAll().stream()
                .filter(m -> (m.getSender() != null && m.getReceiver() != null) &&
                        (Objects.equals(m.getSender().getId(), userId) || Objects.equals(m.getReceiver().getId(), userId)))
                .collect(Collectors.toList());
        Set<User> friends = new HashSet<>();
        for (Message m : personalMessages) {
            if (m.getSender() != null && !Objects.equals(m.getSender().getId(), userId)) friends.add(m.getSender());
            if (m.getReceiver() != null && !Objects.equals(m.getReceiver().getId(), userId)) friends.add(m.getReceiver());
        }
        for (User friend : friends) {
            // Lấy message cuối cùng giữa user và friend
            Optional<Message> lastMsgOpt = personalMessages.stream()
                    .filter(m -> (Objects.equals(m.getSender().getId(), userId) && Objects.equals(m.getReceiver().getId(), friend.getId()))
                            || (Objects.equals(m.getSender().getId(), friend.getId()) && Objects.equals(m.getReceiver().getId(), userId)))
                    .max(Comparator.comparing(Message::getCreatedAt));
            Message lastMsg = lastMsgOpt.orElse(null);
            // Đếm số tin nhắn chưa đọc: chỉ đếm tin nhắn mà receiver là userId và status != SEEN
            int unread = (int) personalMessages.stream()
                    .filter(m -> Objects.equals(m.getSender().getId(), friend.getId()) && Objects.equals(m.getReceiver().getId(), userId)
                            && m.getStatus() != null && m.getStatus() != Message.MessageStatus.SEEN)
                    .count();
            boolean isOnline = presenceService.isOnline(friend.getId());
            boolean isDeleted = lastMsg != null && lastMsg.isDeleted();
            result.add(new ConversationSummary(
                    friend.getId(), false,
                    friend.getFirstName() + " " + friend.getLastName(), friend.getAvatarUrl(),
                    lastMsg != null ? lastMsg.getContent() : null,
                    lastMsg != null && lastMsg.getSender() != null ? lastMsg.getSender().getFirstName() : null,
                    lastMsg != null && lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt().toString() : null,
                    unread, isOnline, isDeleted
            ));
        }

        // 2. Group: lấy các group user là thành viên
//        List<Group> groups = groupRepository.findAll().stream()
//                .filter(g -> g.getCreatedBy() != null && Objects.equals(g.getCreatedBy().getId(), userId))
//                .collect(Collectors.toList());
//        for (Group group : groups) {
//            List<Message> groupMsgs = messageRepository.findAll().stream()
//                    .filter(m -> m.getGroup() != null && Objects.equals(m.getGroup().getId(), group.getId()))
//                    .collect(Collectors.toList());
//            Optional<Message> lastMsgOpt = groupMsgs.stream().max(Comparator.comparing(Message::getCreatedAt));
//            Message lastMsg = lastMsgOpt.orElse(null);
//            int unread = (int) groupMsgs.stream()
//                    .filter(m -> m.getSender() != null && !Objects.equals(m.getSender().getId(), userId)
//                            && m.getStatus() != null && m.getStatus() != Message.MessageStatus.SEEN)
//                    .count();
//            boolean isDeleted = lastMsg != null && lastMsg.isDeleted();
//
//            result.add(new ConversationSummary(
//                    group.getId(), true,
//                    group.getGroupName(), group.getAvatarUrl(),
//                    lastMsg != null ? lastMsg.getContent() : null,
//                    lastMsg != null && lastMsg.getSender() != null ? lastMsg.getSender().getFirstName() : null,
//                    lastMsg != null && lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt().toString() : null,
//                    unread, false, isDeleted
//            ));
//        }
        // Sắp xếp theo thời gian gần nhất
        result.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        return result;
    }

    public List<ConversationSummary> getGroupConversationForUser(User user){
        List<ConversationSummary> result = new ArrayList<>();
        List<GroupMember> members = groupMemberRepository.findByUserAndStatus(user, GroupMember.MemberStatus.ACTIVE);
        for (GroupMember member: members) {
            List<Message>  messages = messageRepository.findByGroupId(member.getGroup().getId());
            Optional<Message> lastMsgOpt = messages.stream().max(Comparator.comparing(Message::getCreatedAt));
            Message lastMsg = lastMsgOpt.orElse(null);

            int unread = 0;

            for (Message m: messages) {
                GroupMessageStatus groupMessageStatus = groupMessageStatusRepository.findByMessage(m);
                if(!groupMessageStatus.getUserIds().contains(user.getId())){
                    unread++;
                }
            }
            boolean isDeleted = lastMsg != null && lastMsg.isDeleted();

            result.add(new ConversationSummary(
                    member.getGroup().getId(), true,
                    member.getGroup().getGroupName(), member.getGroup().getAvatarUrl(),
                    lastMsg != null ? lastMsg.getContent() : null,
                    lastMsg != null && lastMsg.getSender() != null ? lastMsg.getSender().getFirstName() : null,
                    lastMsg != null && lastMsg.getCreatedAt() != null ? lastMsg.getCreatedAt().toString() : null,
                    unread, false, isDeleted
            ));
        }
        result.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return result;

    }

    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {

        return messageRepository.findAll().stream()
            .filter(m -> m.getGroup() == null && m.getSender() != null && m.getReceiver() != null &&
                ((m.getSender().getId().equals(userId1) && m.getReceiver().getId().equals(userId2)) ||
                 (m.getSender().getId().equals(userId2) && m.getReceiver().getId().equals(userId1))))
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .map(m -> {
                if (m.isDeleted()) {
                    Message clone = new Message();
                    clone.setId(m.getId());
                    clone.setDeleted(true);
                    clone.setSender(m.getSender());
                    clone.setReceiver(m.getReceiver());
                    clone.setCreatedAt(m.getCreatedAt());
                    return clone;
                } else {
                    return m;
                }
            })
            .toList();
    }




    public List<Message> getMessagesBetweenUsersPaged(Long userId1, Long userId2, Long beforeMessageId, int size) {
        List<Message> all = messageRepository.findAll().stream()
            .filter(m -> m.getGroup() == null && m.getSender() != null && m.getReceiver() != null &&
                ((m.getSender().getId().equals(userId1) && m.getReceiver().getId().equals(userId2)) ||
                 (m.getSender().getId().equals(userId2) && m.getReceiver().getId().equals(userId1))))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // mới nhất trước
            .map(m -> {
                if (m!= null && m.isDeleted()) {
                    Message clone = new Message();
                    clone.setId(m.getId());
                    clone.setDeleted(true);
                    clone.setSender(m.getSender());
                    clone.setReceiver(m.getReceiver());
                    clone.setCreatedAt(m.getCreatedAt());
                    return clone;
                } else {
                    return m;
                }
            })
            .collect(Collectors.toList());
        if (beforeMessageId != null) {
            Optional<Message> beforeMsg = all.stream().filter(m -> m.getId().equals(beforeMessageId)).findFirst();
            if (beforeMsg.isPresent()) {
                all = all.stream().filter(m -> m.getCreatedAt().isBefore(beforeMsg.get().getCreatedAt())).collect(Collectors.toList());
            }
        }
        return all.stream().limit(size).collect(Collectors.toList());
    }

    public List<Message> getMessagesBetweenUsersPagedGroup(User user, Long groupId, Long beforeMessageId, int size) {
        List<Message> messages = messageRepository.findByGroupId(groupId)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(m -> {
                    if (m!= null && m.isDeleted()) {
                        Message clone = new Message();
                        clone.setId(m.getId());
                        clone.setDeleted(true);
                        clone.setSender(m.getSender());
                        clone.setReceiver(m.getReceiver());
                        clone.setCreatedAt(m.getCreatedAt());
                        return clone;
                    } else {
                        return m;
                    }
                })
                .collect(Collectors.toList());

        if (beforeMessageId != null) {
            Optional<Message> beforeMsg = messages.stream().filter(m -> m.getId().equals(beforeMessageId)).findFirst();
            if (beforeMsg.isPresent()) {
                messages = messages.stream().filter(m -> m.getCreatedAt().isBefore(beforeMsg.get().getCreatedAt())).collect(Collectors.toList());
            }
        }
        return messages.stream().limit(size).collect(Collectors.toList());
    }

    // Khi user mở chat, cập nhật các tin nhắn chưa đọc thành SEEN
    public int markMessagesAsSeen(Long userId, Long friendId, LocalDateTime seenUntil) {
        List<Message> unread = messageRepository.findAll().stream()
            .filter(m -> m.getSender() != null && m.getReceiver() != null
                && Objects.equals(m.getSender().getId(), friendId)
                && Objects.equals(m.getReceiver().getId(), userId)
                && m.getStatus() != null && m.getStatus() != Message.MessageStatus.SEEN
                && (seenUntil == null || (m.getCreatedAt() != null && m.getCreatedAt().isBefore(seenUntil)))
            )
            .collect(Collectors.toList());
        for (Message m : unread) {
            m.setStatus(Message.MessageStatus.SEEN);
            messageRepository.save(m);
        }

        return unread.size();
    }

    public int markMessagesAsSeenGroup(Long userId, Long groupId) {
        int size = 0;
        List<GroupMessageStatus> unread = groupMessageStatusRepository.findByGroupId(groupId);
        for (GroupMessageStatus m : unread) {
            if(!m.getUserIds().contains(userId)){
                m.getUserIds().add(userId);
                groupMessageStatusRepository.save(m);
                size ++;
            }
        }
        return size;
    }

    public ApiResponse<Message> revokeMessage(Long messageId, User user) {
        Message msg = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        if (!msg.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        if (msg.isDeleted()) throw new RuntimeException("Message đã bị thu hồi");
        msg.setDeleted(true);
        return new ApiResponse<>(200, "Thu hồi thành công", LocalDateTime.now(), messageRepository.save(msg));
    }
}
