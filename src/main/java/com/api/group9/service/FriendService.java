package com.api.group9.service;

import com.api.group9.dto.Request.FriendRequest;
import com.api.group9.dto.Response.FriendResponse;
import com.api.group9.enums.FriendStatus;
import com.api.group9.enums.NotificationType;
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
    private NotificationService notificationService;

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

        notificationService.sendNotification(
            sender, 
            receiver, 
            NotificationType.FRIEND_REQUEST, 
            null
        );
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

        if (!friendship.getReceiver().getEmail().equals(myEmail)) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này!");
        }

        friendship.setStatus(FriendStatus.ACCEPTED);
        friendship.setRespondedAt(java.time.Instant.now());
        friendRepo.save(friendship);
       
        notificationService.sendNotification(
            friendship.getReceiver(), 
            friendship.getSender(), 
            NotificationType.FRIEND_ACCEPT, 
            null
        );

        return "Đã trở thành bạn bè!";
    }

    public String acceptRequestByEmail(String myEmail, String targetEmail) {
    User me = userRepo.findByEmail(myEmail).orElseThrow(() -> new RuntimeException("User not found"));
    User target = userRepo.findByEmail(targetEmail).orElseThrow(() -> new RuntimeException("Target not found"));

    FriendShip friendship = friendRepo.findRelationship(me, target)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn nào!"));

    if (friendship.getStatus() == FriendStatus.PENDING) {
        if (friendship.getReceiver().getId().equals(me.getId())) {
            friendship.setStatus(FriendStatus.ACCEPTED);
            friendship.setRespondedAt(java.time.Instant.now());
            friendRepo.save(friendship);
            notificationService.sendNotification(
            friendship.getReceiver(), 
            friendship.getSender(), 
            NotificationType.FRIEND_ACCEPT, 
            null
        );
            return "Đã chấp nhận lời mời!";
        } else {
            throw new RuntimeException("Bạn là người gửi, không thể tự chấp nhận!");
        }
    }
    
    throw new RuntimeException("Trạng thái không hợp lệ để chấp nhận!");
}

    public List<FriendResponse> getPendingRequests(String myEmail) {
        User me = userRepo.findByEmail(myEmail).orElseThrow();

        List<FriendShip> pendingList = friendRepo.findByReceiverAndStatus(me, FriendStatus.PENDING);

        return pendingList.stream().map(f -> {
            FriendResponse dto = new FriendResponse();
            dto.setFriendshipId(f.getId()); 
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

    // Logic giả định trong Backend
    public FriendResponse checkFriendStatus(Long currentUserId, Long targetUserId) {

        FriendResponse response = new FriendResponse();

        User me = userRepo.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User target = userRepo.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        var friendshipOptional = friendRepo.findRelationship(me, target);
        if (friendshipOptional.isEmpty()) {
            response.setStatus("NOT_FRIEND");
            return response;
        }

        FriendShip friendship = friendshipOptional.get();
        
        response.setFriendshipId(friendship.getId());
        response.setFriendSince(friendship.getCreatedAt()); 

        if (friendship.getStatus() == FriendStatus.ACCEPTED) {
            response.setStatus("ACCEPTED");
            return response;
        }

        if (friendship.getStatus() == FriendStatus.PENDING) {
            if (friendship.getSender().getId().equals(currentUserId)) {
                // Mình là Sender -> Đã gửi
                response.setStatus("PENDING");
            } else {
                // Mình là Receiver -> Nhận được
                response.setStatus("RECEIVED");
            }
            return response;
        }

        // Trường hợp lạ khác
        response.setStatus("NOT_FRIEND");
        return response;
    }

    public String unfriend(String myEmail, Long targetId) {
        // Lấy thông tin mình
        User me = userRepo.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Lấy thông tin đối phương
        User target = userRepo.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // Tìm mối quan hệ giữa 2 người (bất kể ai là người gửi/nhận)
        FriendShip friendship = friendRepo.findRelationship(me, target)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mối quan hệ bạn bè!"));

        // Thực hiện xóa khỏi database
        friendRepo.delete(friendship);

        return "Đã xóa kết bạn thành công!";
    }
}