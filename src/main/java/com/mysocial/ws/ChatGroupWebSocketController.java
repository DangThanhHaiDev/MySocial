package com.mysocial.ws;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.message.request.MessageDeletedReactionRequest;
import com.mysocial.dto.message.request.MessageReactionDTO;
import com.mysocial.dto.message.request.MessageReactionRequest;
import com.mysocial.dto.message.request.MessageRequest;
import com.mysocial.dto.message.response.ReactionDeletedResponse;
import com.mysocial.model.GroupMember;
import com.mysocial.model.Message;
import com.mysocial.model.MessageReaction;
import com.mysocial.model.User;
import com.mysocial.repository.MessageReactionRepository;
import com.mysocial.repository.MessageRepository;
import com.mysocial.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ChatGroupWebSocketController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageReactionService messageReactionService;

    @Autowired
    private MessageReactionRepository messageReactionRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private GroupService groupService;

    @MessageMapping("/messages/group")
    public void send(MessageRequest request, org.springframework.messaging.Message<?> message) {
        java.util.Map<String, Object> sessionAttributes = (java.util.Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User sender = userService.findUserProfileByJwt(jwt);
        String fileUrl= null;
        if (request.getFileBase64() != null && request.getFileName() != null) {
            fileUrl = fileService.saveBase64File(request.getFileBase64(), request.getFileName());
        }
        ApiResponse<Message> response = messageService.saveMessage(request, sender, fileUrl);
        // Gửi realtime đến cả sender và receiver qua topic riêng
            String topicReceiver = "/topic/messages/group/" + request.getGroupId();
            messagingTemplate.convertAndSend(topicReceiver, response);

            List<GroupMember> memberList = groupService.getMembers(request.getGroupId());

            //Gửi thông báo đến tất cả các user của cuộc hội thoại
        for (GroupMember member: memberList) {
            messagingTemplate.convertAndSend("/topic/conversations/group/" + member.getUser().getId(), "update");
        }
    }
    @MessageMapping("/messages/group/revoke")
    public void revokeMessage(org.springframework.messaging.Message<?> message, @org.springframework.messaging.handler.annotation.Payload Long messageId) {
        java.util.Map<String, Object> sessionAttributes = (java.util.Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User sender = userService.findUserProfileByJwt(jwt);
        ApiResponse<Message> response = messageService.revokeMessage(messageId, sender);
        Message revoked = response.getData();
            String topicReceiver = "/topic/messages/group/" + revoked.getGroup().getId();

            messagingTemplate.convertAndSend(topicReceiver,
                    java.util.Map.of("action", "revoke", "messageId", revoked.getId()));
        List<GroupMember> memberList = groupService.getMembers(revoked.getGroup().getId());
        for (GroupMember member: memberList) {
            messagingTemplate.convertAndSend("/topic/conversations/group/" + member.getUser().getId(), "update");
        }
    }
    @MessageMapping("/messages/reaction/group")
    public void reactionMessage(
            MessageReactionRequest request,
            org.springframework.messaging.Message<?> message) {

        // Lấy JWT từ session WebSocket
        var sessionAttributes = (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");

        // Tìm người gửi
        User sender = userService.findUserProfileByJwt(jwt);

        // Tạo hoặc cập nhật cảm xúc
        MessageReaction savedReaction = messageReactionService.createEmotion(request, sender);
        Message message1 = messageRepository.findById(request.getMessageId()).orElseThrow();
        List<MessageReaction> messageReactionList = messageReactionRepository.findByMessage(message1);
        // Chuyển sang DTO để gửi về client
        MessageReactionDTO dto = new MessageReactionDTO(
                savedReaction.getMessage().getId(),
                savedReaction.getUser().getId(),
                savedReaction.getUser().getFirstName()+ " "+ savedReaction.getUser().getLastName(),
                savedReaction.getUser().getAvatarUrl(),
                savedReaction.getReaction().getReactionType(),
                messageReactionList
        );

        // Nếu có người nhận thì gửi realtime đến cả 2 phía
        Long receiverId = request.getReceiverId();
            String topicReceiver = "/topic/messages/group/" + request.getGroupId();

            Map<String, Object> payload = Map.of(
                    "action", "reaction",
                    "data", dto
            );

            messagingTemplate.convertAndSend(topicReceiver, payload);


        List<GroupMember> memberList = groupService.getMembers(request.getGroupId());
        for (GroupMember member: memberList) {
            messagingTemplate.convertAndSend("/topic/conversations/group/" + member.getUser().getId(), "update");
        }
    }

    @MessageMapping("/messages/reaction/delete/group")
    public void deleteReaction(
            MessageDeletedReactionRequest request,
            org.springframework.messaging.Message<?> message){
        var sessionAttributes = (Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User sender = userService.findUserProfileByJwt(jwt);



        MessageReaction reactionToDelete = messageReactionRepository.findById(request.getMessageReactionId())
                .orElseThrow(() -> new RuntimeException("Reaction not found"));
        Message message1 = messageRepository.findById(request.getMessageId()).orElseThrow();
        messageReactionService.deleteEmotion(request.getMessageReactionId());


        String topicReceiver = "/topic/messages/group/" + request.getGroupId();
        ReactionDeletedResponse response = new ReactionDeletedResponse();
        response.setReactionId(request.getMessageReactionId());
        response.setMessageId(request.getMessageId());
        Map<String, Object> payload = Map.of(
                "action", "del-reaction",
                "data", response
        );

        messagingTemplate.convertAndSend(topicReceiver, payload);

        // Gửi tín hiệu cập nhật hội thoại
        List<GroupMember> memberList = groupService.getMembers(request.getGroupId());
        for (GroupMember member: memberList) {
            messagingTemplate.convertAndSend("/topic/conversations/group/" + member.getUser().getId(), "react");
        }
    }


}
