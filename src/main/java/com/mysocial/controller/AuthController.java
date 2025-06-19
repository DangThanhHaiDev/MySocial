package com.mysocial.controller;

import com.mysocial.dto.auth.request.LoginRequest;
import com.mysocial.dto.auth.request.RegisterRequest;
import com.mysocial.dto.auth.response.LoginResponse;
import com.mysocial.dto.auth.response.RegisterResponse;
import com.mysocial.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        return new ResponseEntity<>(authService.createUserHandler(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return new ResponseEntity<>(authService.loginUserHandler(request), HttpStatus.OK);
    }

}
