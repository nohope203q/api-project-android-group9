package com.api.group9.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;    // ID hoặc Username người gửi
    private String recipientId; // ID hoặc Username người nhận
    private String content;     // Nội dung tin nhắn
    
    private Date timestamp;

    @PrePersist // Tự động lưu thời gian hiện tại khi insert
    protected void onCreate() {
        timestamp = new Date();
    }
}