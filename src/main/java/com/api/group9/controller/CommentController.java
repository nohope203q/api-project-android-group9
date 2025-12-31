package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.api.group9.dto.Response.CommentResponse; // Import DTO mới
import com.api.group9.model.Comment;
import com.api.group9.service.CommentService;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page, // Mặc định trang 0
            @RequestParam(defaultValue = "10") int size // Mặc định 10 comment/lần
    ) {
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId,
                                              @RequestBody Comment commentRequest) {
        Comment created = commentService.addComment(postId, commentRequest);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id,
                                                 @RequestBody Comment commentRequest) {
        Comment updated = commentService.updateComment(id, commentRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}