package com.api.group9.dto.Response;

import com.api.group9.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Quan trọng: Nếu trường nào null sẽ không trả về JSON
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    
    // Ảnh
    private String profilePictureUrl;
    private String coverUrl;
    
    // Thống kê
    private long friendCount; 
    private long postCount;
    
    // Trạng thái & Token
    private Boolean isVerified;
    private String accessToken; 

    public UserResponse(User user, String accessToken) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.isVerified = user.getIsVerified();
        this.accessToken = accessToken;
    }

    public UserResponse(User user, long friendCount, long postCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.bio = user.getBio();
        this.coverUrl = user.getCoverUrl();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.isVerified = user.getIsVerified();
        this.friendCount = friendCount;
        this.postCount = postCount;
        this.accessToken = null;
    }
}