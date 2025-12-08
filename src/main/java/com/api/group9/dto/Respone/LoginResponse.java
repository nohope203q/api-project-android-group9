package com.api.group9.dto.Respone;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String status;
    private String message;
    
    private String token; 
    
    private UserRespone user; 
}