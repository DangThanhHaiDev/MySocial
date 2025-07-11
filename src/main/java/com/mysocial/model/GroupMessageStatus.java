package com.mysocial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "group_message_status_user_ids", joinColumns = @JoinColumn(name = "group_message_status_id"))
    @Column(name = "user_id")
    private List<Long> userIds = new ArrayList<>();


    @ManyToOne
    private Message message;

}

