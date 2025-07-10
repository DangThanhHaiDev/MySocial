package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.post.PostCreatedResponse;
import com.mysocial.dto.post.PostResponse;
import com.mysocial.dto.Comment.CommentTreeResponse;
import com.mysocial.model.Post;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import com.mysocial.repository.PostReactionRepository;
import com.mysocial.repository.PostRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final String uploadDir = "uploads";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostReactionRepository postReactionRepository;

    public ApiResponse<PostCreatedResponse> createPostHandler(User user, String caption, String location, String imageUrl, Post.Privacy privacy){
        Post post = new Post();
        post.setCreatedAt(LocalDateTime.now());
        post.setDeleted(false);

        post.setContent(caption);
        post.setImage(imageUrl);
        post.setUser(user);
        post.setLocation(location);
        post.setPrivacy(privacy);

        Post postCreated = postRepository.save(post);
        return new ApiResponse<PostCreatedResponse>(201,"Post created successfully", LocalDateTime.now(),
                new PostCreatedResponse(postCreated.getId(), postCreated.getContent(), postCreated.getImage(), postCreated.getLocation(),
                        postCreated.getCreatedAt(), user.getFirstName()+" "+user.getLastName()));
    }
    public List<Post> get(){
       return postRepository.findAll();
    }

    public ApiResponse<List<Post>> getAllPostByUser(User user){
        List posts = postRepository.findPostsByUserId(user.getId());
        ApiResponse response = new ApiResponse(200, "Success", LocalDateTime.now(), posts);
        return response;
    }

    public String deletePostHandler(User user, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()-> new RuntimeException("Post not found"));
        if(!post.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Access deny");
        }
        post.setDeleted(true);
        if(user.getAvatarUrl().equals(post.getImage())){
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
        postRepository.save(post);
        return "Success";
    }

    public List<PostResponse> getAllPostResponses(User currentUser) {
        List<Post> posts = postRepository.findAll();
        List<PostResponse> dtos = new java.util.ArrayList<>();
        for (Post post : posts) {
            dtos.add(toDto(post, currentUser));
        }
        return dtos;
    }

    public List<PostResponse> getAllPostResponsesByUser(User profileUser, User currentUser) {
        List<Post> posts = postRepository.findPostsByUserId(profileUser.getId());
        List<PostResponse> dtos = new java.util.ArrayList<>();
        for (Post post : posts) {
            dtos.add(toDto(post, currentUser));
        }
        return dtos;
    }

    private PostResponse toDto(Post post, User currentUser) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setImage(post.getImage());
        dto.setVideo(post.getVideo());
        dto.setContent(post.getContent());
        dto.setLocation(post.getLocation());
        dto.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
        dto.setDeleted(post.isDeleted());
        dto.setAvatar(post.isAvatar());
        dto.setPrivacy(post.getPrivacy());

        // User info
        PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
        if (post.getUser() != null) {
            userInfo.setId(post.getUser().getId());
            userInfo.setFirstName(post.getUser().getFirstName());
            userInfo.setLastName(post.getUser().getLastName());
            userInfo.setAvatarUrl(post.getUser().getAvatarUrl());
        }
        dto.setUser(userInfo);
        if (post.getComments() != null) {
            java.util.List<CommentTreeResponse> commentDtos = new java.util.ArrayList<>();
            for (com.mysocial.model.Comment comment : post.getComments()) {
                if (comment.getParent() == null && !comment.isDeleted()) {
                    commentDtos.add(commentService.toDto(comment));
                }
            }
            dto.setComments(commentDtos);
        }
        if (currentUser != null) {
            Optional<PostReaction> userReaction = postReactionRepository.findByPostAndUser(post, currentUser);
            if (userReaction.isPresent()) {
                dto.setCurrentUserReactionType(userReaction.get().getReaction().getReactionType());
            } else {
                dto.setCurrentUserReactionType(null);
            }
        }
        int reactionCount = (int)postReactionRepository.countByPost(post);
        dto.setReactionCount(reactionCount);
        return dto;
    }

    public Post createPostAvatar(User user, String imageUrl, String caption, Post.Privacy privacy){
        Post post = new Post();
        post.setAvatar(true);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setDeleted(false);
        post.setImage(imageUrl);
        post.setContent(caption);
        post.setPrivacy(privacy);
        return postRepository.save(post);
    }
}


