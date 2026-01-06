package com.api.group9.service;

import com.api.group9.dto.Response.UserProfileResponse;
import com.api.group9.dto.Response.UserSuggestResponse;
import com.api.group9.model.User;
import com.api.group9.repository.FriendShipRepository;
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired 
    private FriendShipRepository friendshipRepository;

    @Autowired
    private PostRepository PostRepository;
    // 1. Lấy thông tin User (Profile)
    public UserProfileResponse getUserProfile(String identifier) {
        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user nào trùng khớp bro ơi!"));
            long count = friendshipRepository.countFriends(user.getId());
            long postCount = PostRepository.countByUserId(user.getId());
        return new UserProfileResponse(user, count, postCount);
    }

    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        
                long count = friendshipRepository.countFriends(user.getId());
                long postCount = PostRepository.countByUserId(user.getId());
        return new UserProfileResponse(user, count, postCount);
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
        long count = friendshipRepository.countFriends(savedUser.getId());
        long postCount = PostRepository.countByUserId(savedUser.getId());
        // Trả về DTO
        return new UserProfileResponse(savedUser, count, postCount);
    }
    public List<UserSuggestResponse> suggestUsers(String q, int limit) {
        if (q == null) q = "";
        q = q.trim();
        if (q.length() < 1) return List.of();

        int safeLimit = Math.min(Math.max(limit, 1), 20);

        List<User> users = userRepository.suggestUsers(q, PageRequest.of(0, safeLimit));
        return users.stream()
                .map(u -> new UserSuggestResponse(u.getId(), u.getUsername(), u.getFullName(), u.getProfilePictureUrl()))
                .toList();
    }
}