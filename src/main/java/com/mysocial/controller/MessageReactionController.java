package com.mysocial.controller;

import com.mysocial.service.MessageReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message/reaction")
public class MessageReactionController {
    @Autowired
    private MessageReactionService messageReactionService;

    @DeleteMapping("/{id}")
    public void deleteReaction(@PathVariable Long id){
        messageReactionService.deleteEmotion(id);
    }
}
