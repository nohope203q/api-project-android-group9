package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

import com.api.group9.enums.Gender;

@Entity 
@Table(name = "users") 
@Data
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

    private String coverUrl;
    private Boolean isVerified;
    
    private LocalDate dateOfBirth; 
    private String phone;

    @Enumerated(EnumType.STRING) 
    private Gender gender;
    @Column(updatable = false) 
    private Instant createdAt = Instant.now();
    
    private Instant updatedAt = Instant.now();

}