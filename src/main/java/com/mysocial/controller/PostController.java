package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.post.PostCreatedResponse;
import com.mysocial.model.Post;
import com.mysocial.model.User;
import com.mysocial.service.FileService;
import com.mysocial.service.PostService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String jwt,
                                        @RequestParam(value = "file", required = false) MultipartFile file,
                                        @RequestParam(value = "caption", required = true, defaultValue = "") String caption,
                                        @RequestParam(value = "location", required = false, defaultValue = "") String location) throws IOException {
        User user = userService.findUserProfileByJwt(jwt);
        String imageUrl = "";
        if(file != null ){
             imageUrl = fileService.saveImage(file);
        }
        ApiResponse<PostCreatedResponse> response = postService.createPostHandler(user, caption, location, imageUrl);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Post> getAllPosts(@RequestParam("id") Long id){
        return postService.get(id);
    }
}
