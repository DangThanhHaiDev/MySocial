package com.mysocial.service;

import com.mysocial.dto.message.request.MessageReactionRequest;
import com.mysocial.model.Message;
import com.mysocial.model.MessageReaction;
import com.mysocial.model.User;
import com.mysocial.repository.MessageReactionRepository;
import com.mysocial.repository.MessageRepository;
import com.mysocial.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageReactionService {
    @Autowired
    private MessageReactionRepository messageReactionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    public MessageReaction createEmotion(MessageReactionRequest request, User user){
        Message message = messageRepository.findById(request.getMessageId()).orElseThrow();
        MessageReaction messageReaction = messageReactionRepository.findByMessageAndUser(message, user);
        if(messageReaction!=null){
            messageReaction.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow());
            return messageReactionRepository.save(messageReaction);
        }
        MessageReaction m = new MessageReaction();
        m.setReaction(reactionRepository.findById(request.getReactionId()).orElseThrow());
        m.setCreatedAt(LocalDateTime.now());
        m.setUser(user);
        m.setMessage(message);
        return messageReactionRepository.save(m);
    }

    public void deleteEmotion(Long id){

        messageReactionRepository.deleteMessageReaction(id);
    }

}
