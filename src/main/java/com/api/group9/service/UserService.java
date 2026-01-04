package com.api.group9.service;

import com.api.group9.dto.Response.UserProfileResponse;
import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // 1. Lấy thông tin User (Profile)
    public UserProfileResponse getUserProfile(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user nào trùng khớp bro ơi!"));
        
        return new UserProfileResponse(user);
    }

    // 2. Cập nhật thông tin User
    public UserProfileResponse updateUserProfile(String currentUsername, String fullName, String bio, 
                                                 MultipartFile profilePictureUrl, MultipartFile coverUrl) throws IOException {
        
        // Tìm user hiện tại
        User currentUser = userRepository.findByUsernameOrEmail(currentUsername, currentUsername)
                .orElseThrow(() -> new RuntimeException("Lỗi ảo ma: Token ngon nhưng user không thấy đâu!"));

        // Cập nhật thông tin văn bản
        if (fullName != null) currentUser.setFullName(fullName);
        if (bio != null) currentUser.setBio(bio);

        // Xử lý upload ảnh đại diện
        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadImage(profilePictureUrl);
            currentUser.setProfilePictureUrl(avatarUrl);
        }

        // Xử lý upload ảnh bìa
        if (coverUrl != null && !coverUrl.isEmpty()) {
            String coverUrlString = cloudinaryService.uploadImage(coverUrl);
            currentUser.setCoverUrl(coverUrlString);
        }

        // Lưu vào DB
        User savedUser = userRepository.save(currentUser);
        
        // Trả về DTO
        return new UserProfileResponse(savedUser);
    }
}