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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

import com.api.group9.enums.ReactionType; 

import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity 
@Table(name = "reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"}) 
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) 
    private ReactionType type; 
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    public Reaction(Post post, User user, ReactionType type) {
        this.post = post;
        this.user = user;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}