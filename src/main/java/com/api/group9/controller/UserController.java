package com.api.group9.controller;

import com.api.group9.dto.Response.UserResponse;
import com.api.group9.dto.Response.UserSuggestResponse;
import com.api.group9.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{identifier}")
    public ResponseEntity<?> getUserProfile(@PathVariable String identifier) {
        try {
            UserResponse response = userService.getUserProfile(identifier);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile profilePictureUrl,
            @RequestParam(required = false) MultipartFile coverUrl,
            Principal principal // Lấy thông tin người dùng từ Token
    ) {
        try {
            UserResponse response = userService.updateUserProfile(
                principal.getName(), 
                fullName, 
                bio, 
                profilePictureUrl, 
                coverUrl
            );
            
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi upload ảnh rồi bro: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/suggest")
    public List<UserSuggestResponse> suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return userService.suggestUsers(q, limit);
    }
    
}