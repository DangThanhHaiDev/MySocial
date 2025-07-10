package com.mysocial.ws;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.message.request.MessageDeletedReactionRequest;
import com.mysocial.dto.message.request.MessageReactionDTO;
import com.mysocial.dto.message.request.MessageReactionRequest;
import com.mysocial.dto.message.request.MessageRequest;
import com.mysocial.dto.message.response.ReactionDeletedResponse;
import com.mysocial.model.Message;
import com.mysocial.model.MessageReaction;
import com.mysocial.model.User;
import com.mysocial.repository.MessageReactionRepository;
import com.mysocial.repository.MessageRepository;
import com.mysocial.service.FileService;
import com.mysocial.service.MessageReactionService;
import com.mysocial.service.MessageService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class ChatWebSocketController {
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

    @MessageMapping("/messages")
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
        if (request.getReceiverId() != null) {
            String topicSender = "/topic/messages/" + sender.getId() + "/" + request.getReceiverId();
            String topicReceiver = "/topic/messages/" + request.getReceiverId() + "/" + sender.getId();
            messagingTemplate.convertAndSend(topicSender, response);
            messagingTemplate.convertAndSend(topicReceiver, response);
            // Gửi thông báo cập nhật hội thoại cho cả 2 user
            messagingTemplate.convertAndSend("/topic/conversations/" + sender.getId(), "update");
            messagingTemplate.convertAndSend("/topic/conversations/" + request.getReceiverId(), "update");
        }
    }

    @MessageMapping("/messages/revoke")
    public void revokeMessage(org.springframework.messaging.Message<?> message, @org.springframework.messaging.handler.annotation.Payload Long messageId) {
        java.util.Map<String, Object> sessionAttributes = (java.util.Map<String, Object>) message.getHeaders().get("simpSessionAttributes");
        String jwt = (String) sessionAttributes.get("token");
        User sender = userService.findUserProfileByJwt(jwt);
        ApiResponse<Message> response = messageService.revokeMessage(messageId, sender);
        Message revoked = response.getData();
        if (revoked.getReceiver() != null) {
            String topicSender = "/topic/messages/" + sender.getId() + "/" + revoked.getReceiver().getId();
            String topicReceiver = "/topic/messages/" + revoked.getReceiver().getId() + "/" + sender.getId();
            messagingTemplate.convertAndSend(topicSender,
                java.util.Map.of("action", "revoke", "messageId", revoked.getId()));
            messagingTemplate.convertAndSend(topicReceiver,
                java.util.Map.of("action", "revoke", "messageId", revoked.getId()));
        }
    }

    @MessageMapping("/messages/reaction")
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
        if (receiverId != null) {
            String topicSender = "/topic/messages/" + sender.getId() + "/" + receiverId;
            String topicReceiver = "/topic/messages/" + receiverId + "/" + sender.getId();

            Map<String, Object> payload = Map.of(
                    "action", "reaction",
                    "data", dto
            );

            messagingTemplate.convertAndSend(topicSender, payload);
            messagingTemplate.convertAndSend(topicReceiver, payload);

            // Gửi tín hiệu cập nhật hội thoại
            messagingTemplate.convertAndSend("/topic/conversations/" + sender.getId(), "react");
            messagingTemplate.convertAndSend("/topic/conversations/" + receiverId, "react");
        }
    }


    @MessageMapping("/messages/reaction/delete")
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
        String topicSender = "/topic/messages/" + sender.getId() + "/" + request.getReceiverId();
        String topicReceiver = "/topic/messages/" + request.getReceiverId() + "/" + sender.getId();
        ReactionDeletedResponse response = new ReactionDeletedResponse();
        response.setReactionId(request.getMessageReactionId());
        response.setMessageId(request.getMessageId());
        Map<String, Object> payload = Map.of(
                "action", "del-reaction",
                "data", response
        );

        messagingTemplate.convertAndSend(topicSender, payload);
        messagingTemplate.convertAndSend(topicReceiver, payload);

        // Gửi tín hiệu cập nhật hội thoại
        messagingTemplate.convertAndSend("/topic/conversations/" + sender.getId(), "react");
        messagingTemplate.convertAndSend("/topic/conversations/" + request.getReceiverId(), "react");

    }

    // API REST lấy lịch sử tin nhắn giữa 2 user
    @GetMapping("/api/messages/history")
    public List<Message> getHistory(@RequestHeader("Authorization") String jwt,
                                    @RequestParam Long userId,
                                    @RequestParam(required = false) Long beforeMessageId,
                                    @RequestParam(defaultValue = "20") int size) {
        User me = userService.findUserProfileByJwt(jwt);
        LocalDateTime now = LocalDateTime.now();
        messageService.markMessagesAsSeen(me.getId(), userId, now);
        return messageService.getMessagesBetweenUsersPaged(me.getId(), userId, beforeMessageId, size);
    }
}
