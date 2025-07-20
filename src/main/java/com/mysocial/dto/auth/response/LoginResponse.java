package com.mysocial.dto.auth.response;

import com.mysocial.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private int status;
    private String message;
    private String token;
    UserInfoResponse user;
    User.Role role;
}
