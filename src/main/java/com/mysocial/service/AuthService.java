package com.mysocial.service;

import com.mysocial.config.JwtProvider;
import com.mysocial.dto.ApiResponse;
import com.mysocial.dto.auth.request.LoginRequest;
import com.mysocial.dto.auth.request.RegisterRequest;
import com.mysocial.dto.auth.response.LoginResponse;
import com.mysocial.dto.auth.response.RegisterResponse;
import com.mysocial.dto.auth.response.UserInfoResponse;
import com.mysocial.exception.auth.EmailAlreadyExistsException;
import com.mysocial.exception.auth.InvalidCredentialsException;
import com.mysocial.exception.auth.PhoneNumberAlreadyExistsException;
import com.mysocial.mapper.UserMapper;
import com.mysocial.model.User;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDetailsServiceImplement userDetailsService;

    public RegisterResponse createUserHandler(RegisterRequest request){
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already existed with another User");
        }
        if(userRepository.findByPhone(request.getPhone()).isPresent()){
            throw new PhoneNumberAlreadyExistsException("Phone number already existed with another User");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User userSaved = userRepository.save(user);
        user.setStatus("ACTIVE");

        RegisterResponse response = new RegisterResponse();
        response.setStatus(201);
        response.setTimestamp(LocalDateTime.now());
        response.setUser(new UserInfoResponse(userSaved.getId(), userSaved.getPhone(), userSaved.getEmail(), userSaved.getFirstName(),
                userSaved.getLastName(), userSaved.getBirthDate(), userSaved.isGender(), userSaved.getRole(), user.getAvatarUrl(), user.getBiography(), user.getAddress()));
        response.setMessage("User registration successful!");

        return response;
    }

    public LoginResponse loginUserHandler(LoginRequest request){
        Authentication authentication = authenticated(request.getEmail(), request.getPassword());
        String token = jwtProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail()).get();
        if ("BANNED".equals(user.getStatus())) {
            throw new InvalidCredentialsException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ admin.");
        }
        LoginResponse response = new LoginResponse(200, "Login success", token, new UserInfoResponse(user.getId(),user.getPhone(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getBirthDate(), user.isGender(), user.getRole(), user.getAvatarUrl(), user.getBiography(), user.getAddress()), user.getRole());
        return response;

    }

    public ApiResponse logoutUserHandler() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return null;
    }

    public Authentication authenticated(String email, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new InvalidCredentialsException("Invalid username or password");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        return authentication;
    }

}
