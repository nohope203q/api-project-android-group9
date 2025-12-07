package com.api.group9.dto.Request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password; 
    private String fullName;
}