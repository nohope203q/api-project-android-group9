package com.api.group9.dto.Response;

import lombok.Data;
import com.api.group9.model.Story;

@Data
public class StoryResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    
    // Đổi tên thành mediaUrl cho chuẩn (vì có thể là video)
    private String mediaUrl; 
    
    // QUAN TRỌNG: Để Android biết mà hiện ImageView hay ExoPlayer
    private String mediaType; // "IMAGE" hoặc "VIDEO"

    // Thông tin nhạc (nếu có)
    private String musicUrl;
    private String musicTitle;
    private String artistName;

    private String caption;
    private boolean isSeen; 

    public StoryResponse(Story story) {
        this.id = story.getId();
        // Null check cho User để tránh lỗi NullPointerException nếu data bẩn
        if (story.getUser() != null) {
            this.userId = story.getUser().getId();
            this.username = story.getUser().getFullName(); 
            this.userAvatar = story.getUser().getProfilePictureUrl(); 
        }

        // Map dữ liệu Media
        this.mediaUrl = story.getMediaUrl(); // Lấy link ảnh/video từ DB
        
        // Convert Enum sang String cho dễ truyền
        if (story.getMediaType() != null) {
            this.mediaType = story.getMediaType().toString(); 
        } else {
            this.mediaType = "IMAGE"; // Mặc định là ảnh
        }

        // Map dữ liệu Nhạc
        this.musicUrl = story.getMusicUrl();
        this.musicTitle = story.getMusicTitle();
        this.artistName = story.getArtistName();

        this.caption = story.getCaption();
        this.isSeen = false;
    }
}