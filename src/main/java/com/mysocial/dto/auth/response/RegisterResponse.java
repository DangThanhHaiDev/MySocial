package com.mysocial.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private int status;
    private LocalDateTime timestamp;
    private String message;
    private UserInfoResponse user;
}
