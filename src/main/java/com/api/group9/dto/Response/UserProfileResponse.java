package com.api.group9.dto.Response;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.api.group9.model.User;

import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String coverUrl;
    private String profilePictureUrl;
    private String bio;
    private long friendCount; 
    public UserProfileResponse(User user, long friendCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.bio = user.getBio();
        this.coverUrl = user.getCoverUrl();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.friendCount = friendCount;
    }
}
