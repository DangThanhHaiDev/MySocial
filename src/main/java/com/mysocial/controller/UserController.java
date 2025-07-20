package com.mysocial.controller;

import com.mysocial.config.JwtProvider;
import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.auth.response.LoginResponse;
import com.mysocial.dto.auth.response.UserInfoResponse;
import com.mysocial.dto.user.request.UserUpdateRequest;
import com.mysocial.dto.user.response.UserSearchResponse;
import com.mysocial.mapper.UserMapper;
import com.mysocial.model.Post;
import com.mysocial.model.User;
import com.mysocial.service.FileService;
import com.mysocial.service.PostService;
import com.mysocial.service.UserService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FileService fileService;

    @Autowired
    private PostService postService;


    @GetMapping("/me")
    public User getMe(@RequestHeader("Authorization") String jwt) {
        return userService.findUserProfileByJwt(jwt);
    }

    @GetMapping
    public ApiResponse<LoginResponse> getMeProfile(@RequestHeader("Authorization") String jwt){
        User user = userService.findUserProfileByJwt(jwt);
        String token = Jwts.builder()
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime()+10000000))
                .signWith(jwtProvider.getSecretKey())
                .compact();
        LoginResponse response = new LoginResponse();
        response.setUser(new UserInfoResponse(user.getId(), user.getPhone(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(), user.isGender(), user.getRole(),  user.getAvatarUrl(), user.getBiography(), user.getAddress()));
        response.setStatus(200);
        response.setMessage("Get user success");
        response.setToken(token);
        response.setRole(user.getRole());
        return new ApiResponse<>(200, "Get user success", LocalDateTime.now(), response);
    }
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String jwt, @RequestBody UserUpdateRequest request){
        User user = userService.findUserProfileByJwt(jwt);
        User updatedUser = userService.updateUserHandler(user, request);
        UserInfoResponse response = new UserInfoResponse(user.getId(), user.getPhone(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(), user.isGender(), user.getRole(), user.getAvatarUrl(), user.getBiography(), user.getAddress());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId){
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }
    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestHeader("Authorization") String jwt,
                                          @RequestParam(value = "file", required = true) MultipartFile file,
                                          @RequestParam("caption") String caption,
                                          @RequestParam("privacy")Post.Privacy privacy
                                          ) throws IOException {
        User user = userService.findUserProfileByJwt(jwt);
        String imageUrl = "";
        if(file != null ){
            imageUrl = fileService.saveImage(file);
        }
        postService.createPostAvatar(user, imageUrl, caption, privacy);
        return ResponseEntity.ok(userService.updateAvatar(user, imageUrl));
    }
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false, defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.findUserProfileByJwt(jwt);
        UserSearchResponse response = userService.searchUsers(user.getId(), searchTerm, page, size);
        return ResponseEntity.ok(response);
    }
}
