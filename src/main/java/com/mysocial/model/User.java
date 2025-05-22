package com.mysocial.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean gender;
    private String avatarUrl;
    private String phone;
    private String password;
    private LocalDateTime birthDate;
    private String address;
    private String biography;
    private Role role;

    public enum Role{
        CUSTOMER, ADMIN
    }
}
