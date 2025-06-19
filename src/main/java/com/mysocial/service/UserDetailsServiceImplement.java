package com.mysocial.service;

import com.mysocial.exception.auth.InvalidCredentialsException;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
@Service
public class UserDetailsServiceImplement implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.mysocial.model.User> optionalUser;
        com.mysocial.model.User user = null;
        try {
            optionalUser = userRepository.findByEmail(username);
            System.out.println("-----------Đã qua dòng này---------------");
            user = optionalUser.get();
        }catch (Exception e){
            throw new InvalidCredentialsException("User not found with Email: "+username);
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),new ArrayList<GrantedAuthority>());
    }
}
