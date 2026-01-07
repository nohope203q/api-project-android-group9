package com.api.group9.controller;

import com.api.group9.dto.Request.FriendRequest;
import com.api.group9.dto.Response.FriendResponse;
import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;
import com.api.group9.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepo; 

    // 1. API: Check trạng thái (QUAN TRỌNG: Android gọi cái này để hiện nút)
    @GetMapping("/check-status/{targetId}")
    public ResponseEntity<FriendResponse> checkStatus(@PathVariable Long targetId, Principal principal) {
        User me = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Gọi Service (đã trả về FriendResponse)
        FriendResponse response = friendService.checkFriendStatus(me.getId(), targetId);
        
        return ResponseEntity.ok(response);
    }

    // 2. API: Gửi lời mời kết bạn
    @PostMapping("/add") // Trong Android là @POST("friends/request") -> check lại đường dẫn trong Retrofit nhé
    public ResponseEntity<String> addFriend(@RequestBody FriendRequest requestDto, Principal principal) {
        // Lưu ý: Nếu Android gọi "friends/request" thì đổi mapping ở đây thành "/request"
        // Nếu Android gọi "friends/add" thì giữ nguyên
        return ResponseEntity.ok(friendService.sendRequest(principal.getName(), requestDto));
    }

    @PostMapping("/request") 
    public ResponseEntity<String> addFriendAlias(@RequestBody FriendRequest requestDto, Principal principal) {
        return ResponseEntity.ok(friendService.sendRequest(principal.getName(), requestDto));
    }


    // 3. API: Chấp nhận lời mời (Dùng Email - Khớp với Android)
    // Android: @POST("friends/accept") @Query("email")
    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendByEmail(@RequestParam("email") String targetEmail, Principal principal) {
        return ResponseEntity.ok(friendService.acceptRequestByEmail(principal.getName(), targetEmail));
    }

    // 4. API: Lấy danh sách bạn bè
    @GetMapping("/list")
    public ResponseEntity<List<FriendResponse>> getFriendList(Principal principal) {
        return ResponseEntity.ok(friendService.getMyFriends(principal.getName()));
    }

    // 5. API: Lấy danh sách lời mời đang chờ
    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(Principal principal) {
        return ResponseEntity.ok(friendService.getPendingRequests(principal.getName()));
    }

    @DeleteMapping("/unfriend/{targetId}")
    public ResponseEntity<String> unfriend(@PathVariable Long targetId, Principal principal) {
        return ResponseEntity.ok(friendService.unfriend(principal.getName(), targetId));
    }
}