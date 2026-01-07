package com.api.group9.dto.Response;

import com.api.group9.model.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long senderId; 
    
    private String senderName;
    private String senderAvatar;
    private String message;
    private String type;    // LIKE_POST, COMMENT, FRIEND_REQUEST...
    private Long relatedId; // Post ID
    private boolean isRead;
    private String createdAt;

    public NotificationResponse(Notification noti) {
        this.id = noti.getId();
        
        // Gán ID người gửi vào đây
        if (noti.getSender() != null) {
            this.senderId = noti.getSender().getId();
            this.senderName = noti.getSender().getFullName();
            this.senderAvatar = noti.getSender().getProfilePictureUrl();
        }
        
        this.message = noti.getMessage();
        
        // Convert enum to string (tránh null)
        this.type = (noti.getType() != null) ? noti.getType().toString() : "";
        
        this.relatedId = noti.getRelatedId();
        this.isRead = noti.isRead();
        
        // Convert Instant to String
        this.createdAt = (noti.getCreatedAt() != null) ? noti.getCreatedAt().toString() : "";
    }
}