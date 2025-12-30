package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import com.api.group9.dto.Request.PostRequest;
import com.api.group9.model.Post;
import com.api.group9.model.PostImage; // Import Entity ảnh mới
import com.api.group9.model.User;
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));
    }

    // --- HÀM CREATE CHUẨN (Đã gộp và sửa logic ảnh) ---
    public Post createPost(PostRequest request, List<MultipartFile> files) {
        Post post = new Post();

        // 1. Map dữ liệu text
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        post.setPublic(request.isPublic());
        // post.setMusicUrl(request.getMusicUrl());

        // 2. Lấy User từ Token (Dùng Email)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        
        // Gán ID tác giả
        post.setUserId(author.getId());

        // 3. Xử lý Upload ảnh (QUAN TRỌNG: Logic mới cho PostImage)
        if (files != null && !files.isEmpty()) {
            List<PostImage> imageEntities = new ArrayList<>();
            
            for (MultipartFile file : files) {
                try {
                    // A. Upload lên Cloudinary
                    String url = cloudinaryService.uploadImage(file);
                    
                    // B. Tạo Entity PostImage
                    PostImage img = new PostImage();
                    img.setImageUrl(url);
                    img.setPost(post); // BẮT BUỘC: Gắn bài viết vào ảnh
                    
                    imageEntities.add(img);
                } catch (IOException e) {
                    System.err.println("Lỗi upload file: " + file.getOriginalFilename());
                    e.printStackTrace();
                }
            }
            // C. Đưa danh sách ảnh vào Post
            post.setImages(imageEntities);
        }

        return postRepository.save(post);
    }

    // --- HÀM UPDATE ---
    public Post updatePost(Long id, Post updatedPost) {
        // 1. Tìm bài viết cũ
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 2. Kiểm tra quyền (Dùng Email để tìm User hiện tại)
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // So sánh ID người dùng
        if (!existingPost.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // 3. Cập nhật dữ liệu
        if (updatedPost.getContent() != null && !updatedPost.getContent().isEmpty()) {
            existingPost.setContent(updatedPost.getContent());
        }

        if (updatedPost.getLocation() != null) {
            existingPost.setLocation(updatedPost.getLocation());
        }

        existingPost.setPublic(updatedPost.isPublic());

        // Lưu ý: Logic update ảnh thường phức tạp (xóa cũ thêm mới), 
        // tạm thời chưa xử lý ở đây để tránh lỗi.

        return postRepository.save(existingPost);
    }

    public void deletePost(Long id) {
        // Nên kiểm tra quyền trước khi xóa (tương tự update), nhưng tạm thời xóa luôn theo yêu cầu
        Post post = getPostById(id);
        postRepository.delete(post);
    }
}