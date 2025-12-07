package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint; 

import com.api.group9.enums.FriendStatus; // Phải tạo file Enum này

import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends", uniqueConstraints = {
    // Đảm bảo không thể có 2 dòng giống nhau (userId - friendId)
    @UniqueConstraint(columnNames = {"userId", "friendId"}) 
})
@Data
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; 
    @Column(nullable = false)
    private Long friendId; 

    // Trạng thái: PENDING, ACCEPTED, REJECTED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status; 

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime respondedAt; 
}