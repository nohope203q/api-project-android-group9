package com.api.group9.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint; // Import cần thiết cho Index tổng hợp

import com.api.group9.enums.ReactionType; 

import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// @Document chuyển thành @Entity và @Table
@Entity 
// @CompoundIndex chuyển thành @UniqueConstraint trong @Table
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"postId", "userId"}) 
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false)
    private Long postId; 

    @Column(nullable = false)
    private Long userId; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) 
    private ReactionType type; 
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    public Reaction(Long postId, Long userId, ReactionType type) {
        this.postId = postId;
        this.userId = userId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}