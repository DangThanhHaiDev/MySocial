package com.mysocial.controller;

import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
//    @GetMapping("/profile")
//    public ResponseEntity<?>getUserByJwt(@RequestHeader("Authorization") String jwt){
//
//    }
}
