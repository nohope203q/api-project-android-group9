package com.api.group9.service;

import com.api.group9.dto.Request.FriendRequest;
import com.api.group9.dto.Response.FriendResponse;
import com.api.group9.enums.FriendStatus;
import com.api.group9.model.FriendShip;
import com.api.group9.model.User;
import com.api.group9.repository.FriendShipRepository;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendShipRepository friendRepo;

    @Autowired
    private UserRepository userRepo;

    // 1. Gửi lời mời kết bạn
    public String sendRequest(String myEmail, FriendRequest requestDto) {
        User sender = userRepo.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User receiver = userRepo.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Không thể tự kết bạn với chính mình!");
        }

        // Check xem đã có quan hệ gì chưa
        if (friendRepo.findRelationship(sender, receiver).isPresent()) {
            throw new RuntimeException("Đã tồn tại lời mời hoặc đã là bạn bè!");
        }

        FriendShip friendship = new FriendShip();
        friendship.setSender(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendStatus.PENDING); // Trạng thái chờ

        friendRepo.save(friendship);
        return "Gửi lời mời thành công!";
    }

    // 2. Lấy danh sách bạn bè (Map từ Entity -> Response DTO)
    public List<FriendResponse> getMyFriends(String myEmail) {
        User me = userRepo.findByEmail(myEmail).orElseThrow();

        // Lấy list entity từ DB
        List<FriendShip> friendships = friendRepo.findAllFriends(me);

        // Convert sang DTO
        return friendships.stream().map(f -> {
            FriendResponse dto = new FriendResponse();
            dto.setFriendshipId(f.getId());
            dto.setStatus(f.getStatus().name());
            dto.setFriendSince(f.getCreatedAt());

            // Logic quan trọng: Xác định ai là bạn
            // Nếu mình là Sender -> Bạn là Receiver
            // Nếu mình là Receiver -> Bạn là Sender
            User friend = f.getSender().getId().equals(me.getId()) ? f.getReceiver() : f.getSender();

            dto.setId(friend.getId());
            dto.setFullName(friend.getFullName());
            dto.setEmail(friend.getEmail());
            dto.setProfilePictureUrl(friend.getProfilePictureUrl());
            dto.setCoverUrl(friend.getCoverUrl());

            return dto;
        }).collect(Collectors.toList());
    }

    // 3. Chấp nhận lời mời
    public String acceptRequest(String myEmail, Long friendshipId) {
        FriendShip friendship = friendRepo.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời!"));

        // Bảo mật: Chỉ người nhận (Receiver) mới được chấp nhận
        if (!friendship.getReceiver().getEmail().equals(myEmail)) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này!");
        }

        friendship.setStatus(FriendStatus.ACCEPTED);
        friendship.setRespondedAt(java.time.Instant.now());
        friendRepo.save(friendship);

        return "Đã trở thành bạn bè!";
    }

    public List<FriendResponse> getPendingRequests(String myEmail) {
        User me = userRepo.findByEmail(myEmail).orElseThrow();

        // Gọi Repository tìm list pending (Bro tự thêm method vào Repo nhé)
        List<FriendShip> pendingList = friendRepo.findByReceiverAndStatus(me, FriendStatus.PENDING);

        return pendingList.stream().map(f -> {
            FriendResponse dto = new FriendResponse();
            dto.setFriendshipId(f.getId()); // Quan trọng: Trả về ID này để Client gọi API Accept
            dto.setId(f.getSender().getId());
            dto.setFullName(f.getSender().getFullName());
            dto.setEmail(f.getSender().getEmail());
            dto.setStatus(f.getStatus().name());
            dto.setFriendSince(f.getCreatedAt());
            dto.setProfilePictureUrl(f.getSender().getProfilePictureUrl());
            dto.setCoverUrl(f.getSender().getCoverUrl());
            return dto;
        }).collect(Collectors.toList());
    }

    public String checkFriendshipStatus(String myEmail, Long targetUserId) {
    User me = userRepo.findByEmail(myEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
    User target = userRepo.findById(targetUserId)
            .orElseThrow(() -> new RuntimeException("Target user not found"));

    return friendRepo.findRelationship(me, target)
            .map(f -> f.getStatus().name()) 
            .orElse("NOT_FRIEND"); 
}
}