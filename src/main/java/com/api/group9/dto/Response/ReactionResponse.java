package com.api.group9.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionResponse {
    private Long postId;
    private int likeCount;      
    private boolean isLikedByCurrentUser;    
}