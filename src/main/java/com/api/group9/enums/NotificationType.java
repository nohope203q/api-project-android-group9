package com.api.group9.enums;

public enum NotificationType {
    // Thông báo liên quan đến Post và Comment
    POST_LIKED,         // Bài viết được Like
    COMMENT_LIKED,      // Bình luận được Like
    NEW_COMMENT,        // Có bình luận mới trên bài viết của bạn
    
    // Thông báo liên quan đến Friends
    FRIEND_REQUEST,     // Nhận được lời mời kết bạn mới
    FRIEND_ACCEPTED,    // Lời mời kết bạn đã được chấp nhận
    
    // Thông báo khác (Tùy chọn)
    NEW_MESSAGE,        // Có tin nhắn mới
    SYSTEM_ALERT        // Cảnh báo hệ thống
}