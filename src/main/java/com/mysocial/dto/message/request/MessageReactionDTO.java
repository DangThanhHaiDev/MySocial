package com.mysocial.dto.message.request;

import com.mysocial.model.MessageReaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionDTO {
    private Long messageId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String type;
    private List<MessageReaction> reactionList;
}