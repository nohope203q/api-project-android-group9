package com.api.group9.dto.Respone;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    
    private String status; // Ví dụ: "success" hoặc "error"
    private String message;
    
    // Trường này chỉ dùng trong môi trường DEV để kiểm tra OTP
    private String test_otp; 
}