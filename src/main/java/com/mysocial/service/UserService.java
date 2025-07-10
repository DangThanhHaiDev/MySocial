package com.mysocial.service;

import com.mysocial.config.JwtProvider;
import com.mysocial.dto.user.request.UserUpdateRequest;
import com.mysocial.dto.user.response.ProfileResponse;
import com.mysocial.model.User;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    public User findUserProfileByJwt(String jwt){
        String email = jwtProvider.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found with email: "+email));
        return user;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Long getUserIdFromToken(String token) {
        String email = jwtProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    public User updateUserHandler(User user, UserUpdateRequest request){
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.isGender());
        user.setBirthDate(request.getBirthDay());
        user.setAddress(request.getAddress());
        user.setBiography(request.getBio());
        return userRepository.save(user);
    }
    public ProfileResponse getUserById(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        ProfileResponse response = new ProfileResponse();
        response.setUserId(userId);
        response.setBio(user.getBiography());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAddress(user.getAddress());
        response.setBirthDate(user.getBirthDate());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        return response;
    }
    public String updateAvatar(User user, String imageUrl){
        if(imageUrl.equals("")){
            return "Ảnh lỗi";
        }
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return "Update thành công";
    }
}
