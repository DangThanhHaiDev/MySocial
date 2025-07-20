package com.mysocial.util;

import com.mysocial.model.User;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin123@gmail.com";
        if (userRepository.findAll().stream().noneMatch(u -> u.getEmail().equals(adminEmail))) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setPassword(passwordEncoder.encode("Admin@123")); // Đổi mật khẩu nếu muốn
            admin.setRole(User.Role.ADMIN);
            admin.setStatus("ACTIVE");
            userRepository.save(admin);
            System.out.println("Admin account created: " + adminEmail + " / admin123");
        } else {
            System.out.println("Admin account already exists: " + adminEmail);
        }
    }
} 