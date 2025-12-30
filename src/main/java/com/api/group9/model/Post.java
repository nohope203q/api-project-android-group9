package com.api.group9.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>(); 

    @Transient
    private Map<String, Integer> reactionCounts;
    
    @Column(nullable = false)
    private int commentCount = 0;

    private String location;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isPublic = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Helper Methods để Frontend vẫn nhận được List String ---
    
    // 1. Getter giả: Trả về List<String> URL từ List<PostImage>
    // Frontend gọi api sẽ thấy field "imageUrls": ["http...", "http..."]
    public List<String> getImageUrls() {
        if (images == null) return new ArrayList<>();
        return images.stream()
                     .map(PostImage::getImageUrl)
                     .collect(Collectors.toList());
    }

    // 2. Hàm tiện ích để thêm ảnh vào list (Dùng trong Service)
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this); // Gắn ngược lại để Hibernate hiểu
    }
    
}