package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Nhớ import cái này

import com.api.group9.dto.Respone.UserProfileResponse;
import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;
import com.api.group9.service.CloudinaryService; // Import Service vừa tạo

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService; // Inject service vào

    @GetMapping("/profile/{identifier}")
    public ResponseEntity<?> getUserProfile(@PathVariable String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user nào trùng khớp bro ơi!"));
        return ResponseEntity.ok().body(new UserProfileResponse(user));
    }

    // Sửa method này để nhận File
    @PutMapping("/update")
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile profilePictureUrl, // File ảnh đại diện
            @RequestParam(required = false) MultipartFile coverUrl    // File ảnh bìa
    ) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentPrincipalName = authentication.getName();

            User currentUser = userRepository.findByUsernameOrEmail(currentPrincipalName, currentPrincipalName)
                    .orElseThrow(() -> new RuntimeException("Lỗi ảo ma: Token ngon nhưng user không thấy đâu!"));

            // 1. Update text info
            if (fullName != null) currentUser.setFullName(fullName);
            if (bio != null) currentUser.setBio(bio);

            // 2. Xử lý ảnh đại diện (Nếu có gửi lên thì mới up)
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                String avatarUrl = cloudinaryService.uploadImage(profilePictureUrl);
                currentUser.setProfilePictureUrl(avatarUrl); // Lưu link vào DB
            }

            // 3. Xử lý ảnh bìa
            if (coverUrl != null && !coverUrl.isEmpty()) {
                String coverUrlString = cloudinaryService.uploadImage(coverUrl);
                currentUser.setCoverUrl(coverUrlString); // Lưu link vào DB
            }
            
            userRepository.save(currentUser);
            return ResponseEntity.ok().body(new UserProfileResponse(currentUser));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Lỗi upload ảnh rồi bro: " + e.getMessage());
        }
    }
}