package com.mysocial.controller;

import com.mysocial.model.User;
import com.mysocial.service.MessageService;
import com.mysocial.service.UserService;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mysocial.dto.message.response.ConversationSummary;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<ConversationSummary>> getConversations(@RequestHeader("Authorization") String jwt) {
        Long userId = userService.getUserIdFromToken(jwt);
        List<ConversationSummary> conversations = messageService.getConversationsForUser(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/group")
    public ResponseEntity<?> getGroupConversationForUser(@RequestHeader("Authorization") String jwt){
        User user = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(messageService.getGroupConversationForUser(user));
    }
}
