package com.api.group9.model;

import com.api.group9.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // ID của đối tượng liên quan (VD: PostID để bấm vào notification thì mở bài đó ra)
    private Long relatedId; 

    private String message;
    
    private boolean isRead = false; // Đã xem chưa

    private LocalDateTime createdAt = LocalDateTime.now();
}