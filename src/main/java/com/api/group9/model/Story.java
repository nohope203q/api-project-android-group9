package com.api.group9.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Data
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private String mediaUrl; // Link Ảnh hoặc Video
    
    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE hoặc VIDEO

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusHours(24);
        
        if (this.mediaType == null) {
            this.mediaType = MediaType.IMAGE;
        }
    }
    
    public enum MediaType {
        IMAGE, VIDEO
    }
}