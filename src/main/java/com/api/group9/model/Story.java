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

    // --- CẬP NHẬT PHẦN MEDIA ---
    private String mediaUrl; // Link Ảnh hoặc Video
    
    @Enumerated(EnumType.STRING)
    private MediaType mediaType; // IMAGE hoặc VIDEO

    // --- CẬP NHẬT PHẦN NHẠC (Optional) ---
    private String musicUrl;    // Link file nhạc (mp3)
    private String musicTitle;  // Tên bài hát (VD: "Chúng ta của tương lai")
    private String artistName;  // Tên ca sĩ (VD: "Sơn Tùng MTP")

    @Column(columnDefinition = "TEXT")
    private String caption;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plusHours(24);
        
        // Mặc định nếu không set type thì là IMAGE
        if (this.mediaType == null) {
            this.mediaType = MediaType.IMAGE;
        }
    }
    
    // Enum định nghĩa loại media
    public enum MediaType {
        IMAGE, VIDEO
    }
}