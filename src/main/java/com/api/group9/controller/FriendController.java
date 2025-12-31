package com.api.group9.controller;

import com.api.group9.dto.Request.FriendRequest;
import com.api.group9.dto.Response.FriendResponse;
import com.api.group9.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // API: Gửi kết bạn
    @PostMapping("/add")
    public ResponseEntity<String> addFriend(@RequestBody FriendRequest requestDto, Principal principal) {
        return ResponseEntity.ok(friendService.sendRequest(principal.getName(), requestDto));
    }

    // API: Lấy danh sách bạn bè
    @GetMapping("/list")
    public ResponseEntity<List<FriendResponse>> getFriendList(Principal principal) {
        return ResponseEntity.ok(friendService.getMyFriends(principal.getName()));
    }

    // API: Chấp nhận lời mời (truyền ID của friendship vào)
    @PutMapping("/accept/{id}")
    public ResponseEntity<String> acceptFriend(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(friendService.acceptRequest(principal.getName(), id));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(Principal principal) {
        return ResponseEntity.ok(friendService.getPendingRequests(principal.getName()));
    }
}