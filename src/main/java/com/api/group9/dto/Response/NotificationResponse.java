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
    private String senderName;
    private String senderAvatar;
    private String message;
    private String type;     // LIKE_POST, COMMENT...
    private Long relatedId;  // Post ID
    private boolean isRead;
    private String createdAt;

    public NotificationResponse(Notification noti) {
        this.id = noti.getId();
        this.senderName = noti.getSender().getFullName();
        this.senderAvatar = noti.getSender().getProfilePictureUrl();
        this.message = noti.getMessage();
        this.type = noti.getType().toString();
        this.relatedId = noti.getRelatedId();
        this.isRead = noti.isRead();
        this.createdAt = noti.getCreatedAt().toString();
    }
}