package com.mysocial.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String image;
    private String video;
    private String content;
    private String location;
    private LocalDateTime createdAt;
    private boolean isDeleted;
    private boolean isAvatar = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images;

    @OneToMany(mappedBy = "post")
    private List<PostReaction> reactions;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

    @Enumerated
    private Privacy privacy;
    public enum Privacy {
        PUBLIC,
        FRIENDS,
        PRIVATE
    }
}
