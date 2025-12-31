package com.api.group9.dto.Response;
import lombok.Data;
@Data
public class UserResponse {
    private Long id;
    private String bio;
    private String username;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private Boolean isVerified;
    private String accessToken;
}
