package com.api.group9.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    private String content;
    
    private String imageUrl;
    
    private String location;
        
    private boolean isPublic;
    
}