package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity 
@Table(name = "users") 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String fullName;
    
    @Column(columnDefinition = "TEXT") 
    private String bio;

    private String profilePictureUrl;

    private String googleId;

    private String otpCode;
    private LocalDateTime otpExpiry;
    private Boolean isVerified;
    

    @Transient 
    private List<String> friendList; 
    
    @Column(updatable = false) 
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}