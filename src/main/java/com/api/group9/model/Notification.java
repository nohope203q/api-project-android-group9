package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.api.group9.enums.NotificationType; // Phải tạo file Enum này

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID của người nhận thông báo
    @Column(nullable = false)
    private Long recipientId; 
    
    // ID của người/bài viết gây ra thông báo (có thể null nếu là thông báo hệ thống)
    private Long senderId; 

    // Loại thông báo: LIKE_POST, COMMENT_POST, FRIEND_REQUEST, etc.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; 

    @Column(columnDefinition = "TEXT")
    private String content; 

    @Column(nullable = false)
    private boolean isRead = false; 

    @Column(updatable = false)
    private LocalDateTime createdAt;
}