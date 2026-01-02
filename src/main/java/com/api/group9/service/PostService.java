package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import com.api.group9.dto.Request.PostRequest;
import com.api.group9.dto.Response.PostResponse;
import com.api.group9.model.Post;
import com.api.group9.model.PostImage;
import com.api.group9.model.User;
import com.api.group9.model.FriendShip; // Import mới
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.ReactionRepository;
import com.api.group9.repository.UserRepository;
import com.api.group9.repository.FriendShipRepository; // Import mới

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FriendShipRepository friendRepo; // 🔥 Inject thêm cái này để lấy bạn bè
    @Autowired private ReactionRepository reactionRepository;

    // 🔥 Helper: Hàm chuyển từ Entity sang DTO
    private PostResponse mapToResponse(Post post, User author) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setPublic(post.isPublic());
        response.setCreatedAt(post.getCreatedAt());

        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());

        // Map Author
        response.setAuthorId(author.getId());
        response.setAuthorName(author.getFullName());
        response.setAuthorAvatar(author.getProfilePictureUrl());

        // Map Images
        List<String> urls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrl(urls); // Lưu ý: Tên field bên DTO nên là imageUrls (số nhiều)

        return response;
    }

    // API cũ: Lấy tất cả (Dành cho tab Explore/Admin)
    public Page<PostResponse> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable).map(post -> {
            User author = userRepository.findById(post.getUserId()).orElse(new User());
            return mapToResponse(post, author);
        });
    }

    // 🔥 API MỚI: Lấy News Feed chuẩn Facebook (Bài của mình + Bạn bè)
    public Page<PostResponse> getNewsFeed(int page, int size) {
        // 1. Lấy User hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 2. Lấy danh sách ID bạn bè (Từ bảng FriendShip)
        List<FriendShip> friendships = friendRepo.findAllFriends(me);
        
        List<Long> userIds = new ArrayList<>();
        userIds.add(me.getId()); // Thêm chính mình

        for (FriendShip f : friendships) {
            // Logic: Nếu mình là sender -> bạn là receiver, và ngược lại
            User friend = f.getSender().getId().equals(me.getId()) ? f.getReceiver() : f.getSender();
            userIds.add(friend.getId());
        }

        // 3. Query Repo lấy bài viết
        PageRequest pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postRepository.findNewsFeed(userIds, pageable);

        // 4. Map sang DTO + KIỂM TRA LIKE (Đoạn này đã sửa)
        return postsPage.map(post -> {
            User author = userRepository.findById(post.getUserId()).orElse(new User());
            
            // Map các thông tin cơ bản (ảnh, nội dung, tác giả...)
            PostResponse response = mapToResponse(post, author);

            // --- THÊM LOGIC CHECK LIKE Ở ĐÂY ---
            // Kiểm tra trong bảng reactions xem cặp (postId, userId) có tồn tại không
            boolean isLiked = reactionRepository.existsByPostAndUser(post, me);
            
            response.setLikedByCurrentUser(isLiked);
            // ------------------------------------

            return response;
        });
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User author = userRepository.findById(post.getUserId()).orElseThrow();
        return mapToResponse(post, author);
    }

    public PostResponse createPost(PostRequest request, List<MultipartFile> files) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setLocation(request.getLocation());
        post.setPublic(request.isPublic());
        post.setUserId(author.getId());
        // post.setCreatedAt(LocalDateTime.now()); // Entity tự handle hoặc set ở đây

        if (files != null && !files.isEmpty()) {
            // Upload song song (Async)
            List<CompletableFuture<PostImage>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = cloudinaryService.uploadImage(file);
                        PostImage img = new PostImage();
                        img.setImageUrl(url);
                        img.setPost(post);
                        return img;
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi upload ảnh");
                    }
                }))
                .collect(Collectors.toList());

            List<PostImage> images = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            post.setImages(images);
        }

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost, author);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElseThrow();
        
        if (!post.getUserId().equals(currentUser.getId())) {
             throw new AccessDeniedException("Không được xóa bài của người khác!");
        }

        if (post.getImages() != null) {
            for (PostImage img : post.getImages()) {
                try {
                    cloudinaryService.deleteImageByUrl(img.getImageUrl());
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh trên Cloud: " + e.getMessage());
                }
            }
        }
        postRepository.delete(post);
    }
}