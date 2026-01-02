package com.api.group9.dto.Response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private String content;
    private String location;
    private boolean isPublic;
    private LocalDateTime createdAt;
    
    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private List<String> imageUrl;

    private int likeCount;
    private int commentCount;   
    private boolean isLikedByCurrentUser;
}