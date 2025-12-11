package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.group9.dto.Respone.UserRespone;
import com.api.group9.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserRespone> getProfile(Authentication authentication) {
        
        // 1. Lấy ID của người dùng đang đăng nhập từ Security Context
        // (ID này thường được lưu trong JWT Token khi tạo)
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build(); // Chưa đăng nhập
        }

        // Trong trường hợp mầy dùng JWT và lưu ID người dùng trong principal:
        String userIdString = (String) authentication.getPrincipal(); 
        
        // Chuyển đổi ID sang Long để gọi hàm Service
        Long userId;
        try {
            userId = Long.parseLong(userIdString);
        } catch (NumberFormatException e) {
            // Trường hợp ID không phải là số
            return ResponseEntity.status(400).build(); 
        }

        // 2. Gọi hàm Service mà tao đã thêm vào lúc trước
        UserRespone profile = userService.getUserProfile(userId);

        // 3. Trả về kết quả
        return ResponseEntity.ok(profile);
    }
    
    
}
