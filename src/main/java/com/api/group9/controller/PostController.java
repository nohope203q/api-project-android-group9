package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.api.group9.dto.Request.PostRequest;
import com.api.group9.model.Post;
import com.api.group9.service.PostService;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Post> createPost(
            // @RequestPart("post") nhận chuỗi JSON chứa content, location, isPublic...
            @RequestPart("post") PostRequest postRequest, 
            
            // @RequestPart("files") nhận danh sách file ảnh
            @RequestPart(value = "files", required = false) List<MultipartFile> files 
    ) {
        Post createdPost = postService.createPost(postRequest, files);
        return ResponseEntity.ok(createdPost);
    }
    
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post postRequest) {
        Post updated = postService.updatePost(id, postRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

}