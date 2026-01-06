package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.api.group9.model.Post;
import com.api.group9.model.Reaction;
import com.api.group9.model.User;
import com.api.group9.dto.Response.ReactionResponse;
import com.api.group9.dto.Response.UserResponse;
import com.api.group9.enums.NotificationType;
import com.api.group9.enums.ReactionType; // Đảm bảo import đúng Enum
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.ReactionRepository;
import com.api.group9.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Reaction likePost(Long postId) {
        // 1. Lấy Post từ DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 2. Lấy User hiện tại (SỬA: Dùng Email thay vì Username)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 3. Kiểm tra tồn tại
        if (reactionRepository.existsByPostAndUser(post, user)) {
            throw new IllegalStateException("Bạn đã like bài viết này rồi");
        }

        // 4. Tạo Reaction
        Reaction reaction = new Reaction(post, user, ReactionType.LIKE);
        Reaction savedReaction = reactionRepository.save(reaction);

        // 5. CẬP NHẬT SỐ LƯỢNG LIKE VÀO POST (Quan trọng)
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        
        notificationService.sendNotification(
            user, 
            post.getUser(), 
            NotificationType.LIKE_POST, 
            post.getId()
        );

        return savedReaction;
    }

    @Transactional
    public void unlikePost(Long postId) {
        // 1. Lấy User hiện tại (SỬA: Dùng Email)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 2. Lấy Post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 3. Tìm reaction để xóa
        Reaction reaction = reactionRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new NoSuchElementException("Bạn chưa like bài viết này"));

        // 4. Xóa Reaction
        reactionRepository.delete(reaction);

        // 5. CẬP NHẬT SỐ LƯỢNG LIKE VÀO POST (Quan trọng)
        // Dùng Math.max để đảm bảo không bao giờ bị âm
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);
    }

    // Hàm tiện ích đếm số lượng (nếu cần check chéo)
    public long countReactions(Long postId) {
        return reactionRepository.countByPostId(postId);
    }
    public ReactionResponse getReactionStatus(Long postId) {
        // Lấy số lượng
        long count = reactionRepository.countByPostId(postId);
        
        // Kiểm tra User hiện tại có like không
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        
        boolean isLiked = reactionRepository.existsByPostAndUser(post, currentUser);

        return new ReactionResponse(postId, (int) count, isLiked);
    }

    // 2. Hàm mới: Lấy danh sách người like (Cho API Get List)
    public List<UserResponse> getListLikers(Long postId) {
        // Lấy list User từ Repository (Query cũ giữ nguyên)
        List<User> users = reactionRepository.findUsersByPostId(postId);
        
        // Map sang DTO chỉ có id và fullname
        return users.stream().map(user -> {
            UserResponse ur = new UserResponse();
            ur.setId(user.getId());
            ur.setFullName(user.getFullName());
            return ur;
        }).collect(Collectors.toList());
    }
}