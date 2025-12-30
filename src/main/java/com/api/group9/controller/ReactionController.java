package com.api.group9.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.group9.service.ReactionService;

@RestController
@RequestMapping("/api/posts/{postId}/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    // Lấy số lượng like
    @GetMapping
    public ResponseEntity<Long> getReactionsCount(@PathVariable Long postId) {
        long count = reactionService.countReactions(postId);
        return ResponseEntity.ok(count);
    }

    // Like bài viết
    @PostMapping
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        try {
            reactionService.likePost(postId);
            // Trả về 201 Created khi tạo thành công resource mới
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalStateException e) {
            // Trả về 409 Conflict nếu đã like rồi (tránh lỗi 500)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Unlike bài viết
    @DeleteMapping
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        try {
            reactionService.unlikePost(postId);
            // Trả về 204 No Content (chuẩn cho delete thành công)
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}