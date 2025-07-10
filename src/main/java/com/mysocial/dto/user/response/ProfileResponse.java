package com.mysocial.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String bio;
    private String email;
    private String phone;
    private LocalDateTime birthDate;
    private String address;
    private String avatarUrl;
    private String firstName;
    private String lastName;
}
