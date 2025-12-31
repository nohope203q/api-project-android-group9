package com.api.group9.dto.Response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    
    private Long userId;
    private String fullName;
    private String profilePictureUrl;
    private Long parentCommentId;
}