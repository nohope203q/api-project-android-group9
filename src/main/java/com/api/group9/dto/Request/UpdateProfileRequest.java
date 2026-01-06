package com.api.group9.dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String fullName;
    private String bio;
    private String profilePictureUrl;
    private String coverUrl;
    private String phone;
    private String dateOfBirth; 
    private String gender;
}
