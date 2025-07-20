package com.mysocial.controller;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.PagedResponse;
import com.mysocial.dto.post.PostCreatedResponse;
import com.mysocial.dto.post.PostResponse;
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
import java.util.ArrayList;
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
                                        @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                        @RequestParam(value = "caption", required = true, defaultValue = "") String caption,
                                        @RequestParam(value = "location", required = false, defaultValue = "") String location,
                                        @RequestParam(value = "privacy") Post.Privacy privacy
    ) throws IOException {

        User user = userService.findUserProfileByJwt(jwt);
        List<String> imageUrls = new ArrayList<>();
        if(files != null && !files.isEmpty()){
            imageUrls = fileService.saveImages(files);
        }
        ApiResponse<PostCreatedResponse> response = postService.createPostHandler(user, caption, location, imageUrls, privacy);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("")
    public List<PostResponse> getAllPosts(@RequestHeader(value = "Authorization", required = false) String jwt,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "5") int size){
        User user = null;
        if (jwt != null && !jwt.isEmpty()) {
            user = userService.findUserProfileByJwt(jwt);
        }
        return postService.getUserFeed(user, page, size);
    }

    @GetMapping("/user")
    public ResponseEntity<PagedResponse<PostResponse>> getPostByUser(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User user = userService.findUserProfileByJwt(jwt);
        PagedResponse<PostResponse> response = postService.getAllPostResponsesByUser(user, user, page, size);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestHeader("Authorization") String jwt){
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(postService.deletePostHandler(user, id), HttpStatus.OK);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostProfile(@PathVariable Long userId, @RequestHeader(value = "Authorization", required = false) String jwt,
                                                             @RequestParam("page") int page,
                                                             @RequestParam("size") int size){
        User profileUser = userService.findUserById(userId);
        User currentUser = null;
        if (jwt != null && !jwt.isEmpty()) {
            currentUser = userService.findUserProfileByJwt(jwt);
        }
        return ResponseEntity.ok(postService.getAllPostResponsesByAnotherUser(profileUser, currentUser, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String jwt) {
        Post post = postService.getPostById(id);
        PostResponse dto = postService.toDto(post, null); // null nếu không cần currentUser
        return ResponseEntity.ok(dto);
    }
}
