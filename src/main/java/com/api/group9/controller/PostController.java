package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.group9.dto.Request.PostRequest;
import com.api.group9.dto.Request.PostUpdateRequest;
import com.api.group9.dto.Response.PostResponse;
import com.api.group9.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // 1. TẠO BÀI VIẾT
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            // Dùng @ModelAttribute để Postman gửi dạng form-data (Key-Value) dễ dàng hơn
            @ModelAttribute PostRequest postRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(postService.createPost(postRequest, files));
    }

    // 2. LẤY NEWS FEED
    @GetMapping("/newsfeed") // Thêm path để rõ ràng hơn, tránh conflict với getAll
    public ResponseEntity<Page<PostResponse>> getNewsFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getNewsFeed(page, size));
    }

    // 3. LẤY CHI TIẾT BÀI VIẾT
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        // Trả về JSON thay vì String
        return ResponseEntity.ok().body(java.util.Collections.singletonMap("message", "Đã xóa bài viết!"));
    }

    // 5. LẤY BÀI VIẾT CỦA 1 USER (PROFILE)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    // 6. CẬP NHẬT BÀI VIẾT (SỬA LỖI CHÍNH Ở ĐÂY)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @ModelAttribute PostUpdateRequest request,
            // Đổi tên thành "files" cho đồng bộ với Create
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // Service đã trả về PostResponse, không cần map lại thủ công ở đây
        PostResponse response = postService.updatePost(id, request, files);

        return ResponseEntity.ok(response);
    }
}