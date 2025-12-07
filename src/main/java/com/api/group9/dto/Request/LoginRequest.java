package com.api.group9.dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Tự động tạo Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // Tạo constructor không tham số
@AllArgsConstructor // Tạo constructor với tất cả các trường
public class LoginRequest {
    
    // Có thể là username hoặc email (tùy logic backend)
    private String email; 
    
    // Mật khẩu chưa mã hóa
    private String password; 
}