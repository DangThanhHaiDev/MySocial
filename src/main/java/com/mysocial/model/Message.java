package com.mysocial.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String imageUrl;
    private String videoUrl;
    private String fileUrl;
    private boolean isDeleted;
    private boolean isEdited;


    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Message replyTo;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL,  fetch = FetchType.EAGER)
    private List<MessageReaction> reactions;

    public enum MessageStatus{
        SENT,
        DELIVERED,
        SEEN
    }
    public enum MessageType{
        TEXT,
        IMAGE,
        FILE
    }
}
