package com.api.group9.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.group9.dto.Response.ReactionResponse; // Import DTO mới
import com.api.group9.dto.Response.UserLikerResponse;
import com.api.group9.service.ReactionService;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    // API 1: Lấy thông tin tổng quan (Số lượng + Nút xanh/trắng)
    // URL: GET /api/posts/{id}/reactions
    @GetMapping
    public ResponseEntity<ReactionResponse> getReactionStatus(@PathVariable Long postId) {
        ReactionResponse response = reactionService.getReactionStatus(postId);
        return ResponseEntity.ok(response);
    }

    // API 2: Lấy danh sách chi tiết người đã like
    // URL: GET /api/posts/{id}/reactions/list
    @GetMapping("/list")
    public ResponseEntity<List<UserLikerResponse>> getListLikers(@PathVariable Long postId) {
        List<UserLikerResponse> likers = reactionService.getListLikers(postId);
        return ResponseEntity.ok(likers);
    }

    // 2. Like bài viết -> Trả về số lượng mới + trạng thái true
    @PostMapping
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        try {
            reactionService.likePost(postId);
            
            // Lấy lại số lượng mới nhất để trả về cho Client vẽ lại giao diện
            long newCount = reactionService.countReactions(postId);
            
            return ResponseEntity.ok(new ReactionResponse(
                postId, 
                (int) newCount, 
                true, // isLiked = true
                "Like thành công"
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Unlike bài viết -> Trả về số lượng mới + trạng thái false
    @DeleteMapping
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        try {
            reactionService.unlikePost(postId);

            // Lấy lại số lượng mới nhất
            long newCount = reactionService.countReactions(postId);

            return ResponseEntity.ok(new ReactionResponse(
                postId, 
                (int) newCount, 
                false, // isLiked = false
                "Bỏ like thành công"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}