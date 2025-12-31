package com.api.group9.dto.Response; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private Long friendshipId;   // ID của mối quan hệ (để dùng khi hủy kết bạn/chặn)
    private Long id;             // ID của thằng bạn
    private String fullName;
    private String email;
    private String coverUrl;
    private String profilePictureUrl;   
    private String status;       // ACCEPTED, PENDING...
    private Instant friendSince; // Ngày kết bạn
}