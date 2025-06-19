package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.post.PostCreatedResponse;
import com.mysocial.model.Post;
import com.mysocial.model.User;
import com.mysocial.repository.PostRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final String uploadDir = "uploads";
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public ApiResponse<PostCreatedResponse> createPostHandler(User user, String caption, String location, String imageUrl){
        Post post = new Post();
        post.setCreatedAt(LocalDateTime.now());
        post.setDeleted(false);

        post.setContent(caption);
        post.setImage(imageUrl);
        post.setUser(user);
        post.setLocation(location);

        Post postCreated = postRepository.save(post);
        return new ApiResponse<PostCreatedResponse>(201,"Post created successfully", LocalDateTime.now(),
                new PostCreatedResponse(postCreated.getId(), postCreated.getContent(), postCreated.getImage(), postCreated.getLocation(),
                        postCreated.getCreatedAt(), user.getFirstName()+" "+user.getLastName()));
    }
    public List<Post> get(Long userId){
       return postRepository.findPostsByUserId(userId);
    }
}
