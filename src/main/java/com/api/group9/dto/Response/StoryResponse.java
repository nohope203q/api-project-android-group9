package com.api.group9.dto.Response;

import lombok.Data;
import com.api.group9.model.Story;

@Data
public class StoryResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    
    private String mediaUrl; 
    
    private String mediaType; 

    private String caption;
    private boolean isSeen; 

    public StoryResponse(Story story) {
        this.id = story.getId();
        if (story.getUser() != null) {
            this.userId = story.getUser().getId();
            this.username = story.getUser().getFullName(); 
            this.userAvatar = story.getUser().getProfilePictureUrl(); 
        }

        // Map dữ liệu Media
        this.mediaUrl = story.getMediaUrl(); 
        
        if (story.getMediaType() != null) {
            this.mediaType = story.getMediaType().toString(); 
        } else {
            this.mediaType = "IMAGE"; // Mặc định là ảnh
        }

        this.caption = story.getCaption();
        this.isSeen = false;
    }
}