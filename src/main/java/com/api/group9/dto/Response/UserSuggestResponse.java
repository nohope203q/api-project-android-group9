package com.api.group9.dto.Response;

public class UserSuggestResponse {
    private Long id;
    private String username;
    private String fullName;
    private String avatarUrl;

    public UserSuggestResponse(Long id, String username, String fullName, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}