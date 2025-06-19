package com.mysocial.service;

import com.mysocial.config.JwtProvider;
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
}
