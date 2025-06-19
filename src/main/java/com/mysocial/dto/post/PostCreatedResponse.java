package com.mysocial.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreatedResponse {
    private Long id;
    private String caption;
    private String imageUrl;
    private String location;
    private LocalDateTime createdAt;
    private String fullName;
}
