package com.api.group9.dto.Respone;

import com.api.group9.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String status;
    private String message;
    
    // Token JWT (hoặc chuỗi mã hóa phiên làm việc)
    private String token; 
    
    // Thông tin cơ bản của người dùng (tùy chọn)
    private User user; 
}