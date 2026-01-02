package com.api.group9.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.BatchSize; // Import thêm cái này để tối ưu ảnh

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false) 
    private User user;
    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10) // <--- Thêm dòng này để load ảnh nhanh hơn, tránh N+1
    private List<PostImage> images = new ArrayList<>(); 

    @Column(nullable = false)
    private int likeCount = 0;
    
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

    public List<String> getImageUrls() {
        if (images == null) return new ArrayList<>();
        return images.stream()
                      .map(PostImage::getImageUrl)
                      .collect(Collectors.toList());
    }

    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this); 
    }
}