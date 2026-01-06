package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import com.api.group9.dto.Request.PostRequest;
import com.api.group9.dto.Request.PostUpdateRequest;
import com.api.group9.dto.Response.PostResponse;
import com.api.group9.model.Post;
import com.api.group9.model.PostImage;
import com.api.group9.model.User;
import com.api.group9.model.FriendShip;
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.ReactionRepository;
import com.api.group9.repository.UserRepository;
import com.api.group9.repository.CommentRepository;
import com.api.group9.repository.FriendShipRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FriendShipRepository friendRepo; 
    @Autowired private ReactionRepository reactionRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReactionRepository likeRepository;
    
    private PostResponse mapToResponse(Post post, User author) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setPublic(post.isPublic());
        response.setCreatedAt(post.getCreatedAt());

        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());

        if (author != null) {
            response.setAuthorId(author.getId());
            response.setAuthorName(author.getFullName());
            response.setAuthorAvatar(author.getProfilePictureUrl());
        }

        // Lấy danh sách URL ảnh
        List<String> urls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImageUrl(urls); 

        // Kiểm tra xem User đang login có like bài này chưa
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userRepository.findByEmail(email).ifPresent(currentUser -> {
                boolean isLiked = reactionRepository.existsByPostAndUser(post, currentUser);
                response.setLikedByCurrentUser(isLiked);
            });
        } catch (Exception e) {
            // Ignored: Trường hợp guest xem hoặc lỗi context
            response.setLikedByCurrentUser(false);
        }

        return response;
    }

    // --- 1. LẤY TẤT CẢ BÀI VIẾT (ADMIN/DEBUG) ---
    public Page<PostResponse> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable).map(post -> {
            User author = userRepository.findById(post.getUserId()).orElse(new User());
            return mapToResponse(post, author);
        });
    }

    // --- 2. LẤY BÀI VIẾT THEO USER ID (PROFILE) ---
    public List<PostResponse> getPostsByUserId(Long targetUserId) {
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(targetUserId);
        
        // Tìm User author 1 lần (tối ưu hơn query trong loop, nhưng code ngắn gọn thì để trong loop cũng được)
        User author = userRepository.findById(targetUserId).orElse(new User());

        return posts.stream().map(post -> mapToResponse(post, author))
                    .collect(Collectors.toList());
    }

    // --- 3. LẤY NEWS FEED (CỦA MÌNH + BẠN BÈ) ---
    public Page<PostResponse> getNewsFeed(int page, int size) {
        // Lấy User hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Lấy danh sách ID bạn bè
        List<FriendShip> friendships = friendRepo.findAllFriends(me);
        
        List<Long> userIds = new ArrayList<>();
        userIds.add(me.getId()); // Thêm chính mình

        for (FriendShip f : friendships) {
            User friend = f.getSender().getId().equals(me.getId()) ? f.getReceiver() : f.getSender();
            userIds.add(friend.getId());
        }

        // Query DB
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Giả sử repository có hàm findByUserIdIn(List<Long> ids, Pageable p)
        // Nếu dùng @Query custom tên findNewsFeed thì giữ nguyên
        Page<Post> postsPage = postRepository.findNewsFeed(userIds, pageable); 

        return postsPage.map(post -> {
            User author = userRepository.findById(post.getUserId()).orElse(new User());
            return mapToResponse(post, author);
        });
    }

    // --- 4. LẤY CHI TIẾT 1 BÀI ---
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User author = userRepository.findById(post.getUserId()).orElse(new User());
        return mapToResponse(post, author);
    }

    // --- 5. TẠO BÀI VIẾT MỚI ---
    @Transactional
    public PostResponse createPost(PostRequest request, List<MultipartFile> files) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setPublic(request.isPublic());
        post.setUserId(author.getId());
        // post.setCreatedAt được xử lý bởi @PrePersist trong Entity

        // Xử lý upload ảnh (nếu có)
        if (files != null && !files.isEmpty()) {
            List<PostImage> images = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    String url = cloudinaryService.uploadImage(file);
                    PostImage img = new PostImage();
                    img.setImageUrl(url);
                    img.setPost(post); // Gán quan hệ
                    images.add(img);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage());
                }
            }
            post.setImages(images);
        }

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost, author);
    }

    // --- 6. XÓA BÀI VIẾT ---
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentEmail).orElseThrow();
        
        // Check quyền: Chỉ chủ bài viết mới được xóa
        if (!post.getUserId().equals(currentUser.getId())) {
             throw new AccessDeniedException("Không được xóa bài của người khác!");
        }

        // Xóa ảnh trên Cloudinary trước
        if (post.getImages() != null) {
            for (PostImage img : post.getImages()) {
                try {
                    cloudinaryService.deleteImageByUrl(img.getImageUrl());
                } catch (Exception e) {
                    System.err.println("Lỗi xóa ảnh trên Cloud: " + e.getMessage());
                }
            }
        }
        
        likeRepository.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    // --- 7. CẬP NHẬT BÀI VIẾT (FULL LOGIC) ---
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, List<MultipartFile> files) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Bài viết không tồn tại với ID: " + postId));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // 1. Update text
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        
        // Có thể update thêm privacy nếu DTO có
        // if (request.isPublic() != null) post.setPublic(request.isPublic());

        // 2. Update Ảnh (Nếu có gửi ảnh mới lên -> Thay thế hoàn toàn)
        if (files != null && !files.isEmpty()) {
            // A. Xóa ảnh cũ trên Cloudinary
            for (PostImage oldImg : post.getImages()) {
                try {
                    cloudinaryService.deleteImageByUrl(oldImg.getImageUrl());
                } catch (Exception e) {
                    // Log lỗi nhưng không chặn process
                    System.err.println("Cloud delete error: " + e.getMessage());
                }
            }

            // B. Xóa ảnh cũ trong DB
            post.getImages().clear();

            // C. Upload và thêm ảnh mới
            for (MultipartFile file : files) {
                try {
                    String newUrl = cloudinaryService.uploadImage(file);
                    PostImage newImage = new PostImage();
                    newImage.setImageUrl(newUrl);
                    newImage.setPost(post);
                    
                    post.getImages().add(newImage);
                } catch (Exception e) {
                    throw new RuntimeException("Lỗi upload ảnh mới: " + e.getMessage());
                }
            }
        }

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost, savedPost.getUser());
    }
}