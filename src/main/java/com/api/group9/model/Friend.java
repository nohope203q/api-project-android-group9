package com.api.group9.model;

import com.api.group9.enums.FriendStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant; // Dùng Instant

@Entity
@Table(name = "friendships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"}) 
})
@Data
@NoArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; 

    // Trạng thái: PENDING, ACCEPTED, REJECTED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10) // Giới hạn độ dài cho Enum
    private FriendStatus status; 

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
    
    private Instant respondedAt; 
}