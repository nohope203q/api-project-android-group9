package com.api.group9.dto.Response;

import com.api.group9.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Quan trọng: Nếu trường nào null sẽ không trả về JSON
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    
    // Ảnh
    private String profilePictureUrl;
    private String coverUrl;
    
    // Thống kê
    private long friendCount; 
    private long postCount;
    
    // Trạng thái & Token
    private Boolean isVerified;
    private String accessToken; // Chỉ dùng khi đăng nhập/đăng ký

    // --- CÁC TRƯỜNG MỚI CHO EDIT PROFILE ---
    private String phone;
    private String dateOfBirth;
    private String gender;

    // ==========================================================
    // CONSTRUCTOR 1: Dùng cho LOGIN / REGISTER (Có Token, không cần đếm friend/post)
    // ==========================================================
    public UserResponse(User user, String accessToken) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.isVerified = user.getIsVerified();
        this.accessToken = accessToken;
        
        // Map các trường phụ nếu có
        mapProfileFields(user);
    }

    // ==========================================================
    // CONSTRUCTOR 2: Dùng cho XEM PROFILE (Có friendCount, postCount, không cần Token)
    // ==========================================================
    public UserResponse(User user, long friendCount, long postCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.bio = user.getBio();
        this.coverUrl = user.getCoverUrl();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.isVerified = user.getIsVerified();
        
        // Gán số lượng
        this.friendCount = friendCount;
        this.postCount = postCount;
        
        // Token null để @JsonInclude ẩn đi
        this.accessToken = null;

        // Map các trường phụ
        mapProfileFields(user);
    }

    // Hàm phụ để map các trường thông tin cá nhân (tránh viết lại code)
    private void mapProfileFields(User user) {
        this.phone = user.getPhone();
        
        if (user.getDateOfBirth() != null) {
            this.dateOfBirth = user.getDateOfBirth().toString(); // YYYY-MM-DD
        }
        
        if (user.getGender() != null) {
            this.gender = user.getGender().name(); // "MALE", "FEMALE"
        }
    }
}