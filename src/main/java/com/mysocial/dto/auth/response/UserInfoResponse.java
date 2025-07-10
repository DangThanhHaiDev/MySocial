package com.mysocial.dto.auth.response;

import com.mysocial.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String phone;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime birthDate;
    private boolean gender;
    private User.Role role;
    private String avatarUrl;
    private String biography;
    private String address;
}
