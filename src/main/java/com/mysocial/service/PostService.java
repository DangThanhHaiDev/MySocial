package com.mysocial.service;

import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.PagedResponse;
import com.mysocial.dto.post.PostCreatedResponse;
import com.mysocial.dto.post.PostResponse;
import com.mysocial.dto.Comment.CommentTreeResponse;
import com.mysocial.model.Friendship;
import com.mysocial.model.Post;
import com.mysocial.model.PostImage;
import com.mysocial.model.PostReaction;
import com.mysocial.model.User;
import com.mysocial.repository.FriendshipRepository;
import com.mysocial.repository.PostImageRepository;
import com.mysocial.repository.PostReactionRepository;
import com.mysocial.repository.PostRepository;
import com.mysocial.repository.UserRepository;
import com.mysocial.util.BadWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    public ApiResponse<PostCreatedResponse> createPostHandler(User user, String caption, String location, List<String> imageUrls, Post.Privacy privacy){
        if (BadWordFilter.containsBadWords(caption)) {
            throw new RuntimeException("Nội dung bài viết chứa từ ngữ không phù hợp!");
        }
        Post post = new Post();
        post.setCreatedAt(LocalDateTime.now());
        post.setDeleted(false);
        post.setContent(caption);
        post.setUser(user);
        post.setLocation(location);
        post.setPrivacy(privacy);
        // Lưu nhiều ảnh
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<PostImage> images = new ArrayList<>();
            for (String url : imageUrls) {
                PostImage img = new PostImage();
                img.setUrl(url);
                img.setPost(post);
                images.add(img);
            }
            post.setImages(images);
        }
        Post postCreated = postRepository.save(post);
        return new ApiResponse<PostCreatedResponse>(201,"Post created successfully", LocalDateTime.now(),
                new PostCreatedResponse(postCreated.getId(), postCreated.getContent(), null, postCreated.getLocation(),
                        postCreated.getCreatedAt(), user.getFirstName()+" "+user.getLastName()));
    }
    public List<Post> get(){
       return postRepository.findAll();
    }

//    public ApiResponse<List<Post>> getAllPostByUser(User user){
//        List posts = postRepository.findPostsByUserId(user.getId());
//        ApiResponse response = new ApiResponse(200, "Success", LocalDateTime.now(), posts);
//        return response;
//    }

    public String deletePostHandler(User user, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()-> new RuntimeException("Post not found"));
        if(!post.getUser().getId().equals(user.getId())){
            throw new RuntimeException("Access deny");
        }
        post.setDeleted(true);
        if(user.getAvatarUrl()!=null){
            if(user.getAvatarUrl().equals(post.getImage())){
                user.setAvatarUrl(null);
                userRepository.save(user);
            }
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

    public List<PostResponse> getUserFeed(User user, int page, int size) {
        List<User> friends = friendshipRepository.findFriendsOf(user.getId());
        List<Long> friendIds = friends.stream().map((f)->f.getId()).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);

        List<Post> posts = postRepository.findFeedPosts(user.getId(), friendIds, pageable);
        List<PostResponse> dtos = new java.util.ArrayList<>();
        for (Post post : posts) {
            dtos.add(toDto(post, user));
        }
        return dtos;
    }

    public PagedResponse<PostResponse> getAllPostResponsesByUser(User profileUser, User currentUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findPostsByUserId(profileUser.getId(), pageable);

        List<PostResponse> dtos = new ArrayList<>();
        for (Post post : postsPage.getContent()) {
            dtos.add(toDto(post, currentUser));
        }

        return new PagedResponse<>(dtos, postsPage);
    }

    public PagedResponse<PostResponse> getAllPostResponsesByAnotherUser(User profileUser, User currentUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Friendship friendship = friendshipRepository.findRelationship(profileUser.getId(), currentUser.getId());

        List<Post> filteredPosts;

        if (friendship == null || friendship.getStatus() == Friendship.Status.PENDING) {
            filteredPosts = postRepository.findPostsByUserIdAndRelated(profileUser.getId(), Post.Privacy.PUBLIC);
        } else if (friendship.getStatus() == Friendship.Status.ACCEPTED) {
            // Lấy tất cả rồi lọc PRIVATE
            filteredPosts = postRepository.findByUserAndIsDeletedFalse(profileUser.getId())
                    .stream()
                    .filter(p -> !p.getPrivacy().equals(Post.Privacy.PRIVATE))
                    .collect(Collectors.toList());
        } else {
            filteredPosts = Collections.emptyList();
        }

        // Phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredPosts.size());
        List<Post> pagedList = start > end ? Collections.emptyList() : filteredPosts.subList(start, end);
        Page<Post> pageResult = new PageImpl<>(pagedList, pageable, filteredPosts.size());

        List<PostResponse> dtos = pagedList.stream()
                .map(post -> toDto(post, currentUser))
                .collect(Collectors.toList());

        return new PagedResponse<>(dtos, pageResult);
    }

    public PostResponse toDto(Post post, User currentUser) {
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
        // Trả về list url ảnh
        if (post.getImages() != null) {
            List<String> imageUrls = post.getImages().stream().map(PostImage::getUrl).toList();
            dto.setImages(imageUrls);
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

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
    }
}


