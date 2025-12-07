package com.api.group9.dto.Respone;
import lombok.Data;
@Data
public class UserRespone {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private Boolean isVerified;


}
