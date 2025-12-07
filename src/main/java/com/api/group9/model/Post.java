package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Đổi @Document sang @Entity và @Table
@Entity 
@Table(name = "posts") 
@Data
@NoArgsConstructor
@AllArgsConstructor 
public class Post { // Đổi tên class thành Post để nhất quán với Entity

    // @Id (String) chuyển sang @Id (Long tự tăng)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    // ID của User phải là Long để tham chiếu đến bảng users
    @Column(nullable = false)
    private Long userId; 

    // Nội dung bài viết nên dùng TEXT
    @Column(columnDefinition = "TEXT") 
    private String content; 

    // Các trường phức tạp (List và Map) cần xử lý đặc biệt
    @Transient // Tạm thời bỏ qua, không ánh xạ vào cột
    private List<String> imageUrls; 
    
    private String musicUrl; // Vẫn là String bình thường

    @Transient // Tạm thời bỏ qua, cần tạo bảng riêng (hoặc dùng JSON)
    private Map<String, Integer> reactionCounts; 
    
    @Column(nullable = false)
    private int commentCount = 0; 

    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private String location; 

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isPublic = true;

    // Cập nhật Constructor để dùng Long cho userId
    public Post(Long userId, String content, List<String> imageUrls, String location) {
        this.userId = userId;
        this.content = content;
        this.imageUrls = imageUrls;
        this.location = location;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}