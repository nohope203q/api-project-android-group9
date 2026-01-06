package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import com.api.group9.dto.Response.CommentResponse; // Import DTO mới
import com.api.group9.enums.NotificationType;
import com.api.group9.model.Comment;
import com.api.group9.model.Post;
import com.api.group9.model.User;
import com.api.group9.repository.CommentRepository;
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // --- SỬA ĐỔI: Phân trang và trả về DTO chứa thông tin User ---
    public Page<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        // 1. Tạo Pageable: Sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 2. Query DB
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        // 3. Map sang DTO
        return commentPage.map(this::mapToResponse);
    }

    // Hàm phụ trợ để map Entity -> DTO
    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse res = new CommentResponse();
        res.setId(comment.getId());
        res.setContent(comment.getContent());
        res.setCreatedAt(comment.getCreatedAt());
        res.setUserId(comment.getUserId());

        res.setParentCommentId(comment.getParentCommentId());
        // Lấy thông tin User để hiển thị Avatar/Tên
        userRepository.findById(comment.getUserId()).ifPresent(user -> {
            res.setFullName(user.getFullName());
            res.setProfilePictureUrl(user.getProfilePictureUrl());
        });

        return res;
    }

    @Transactional
    public Comment addComment(Long postId, Comment comment) {
        // 1. Kiểm tra Post có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 2. Lấy User từ Token (Tìm bằng Email)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        // Kiểm tra nếu là trả lời bình luận thì phải kiểm tra bình luận cha
        if (comment.getParentCommentId() != null) {
            // Kiểm tra xem bình luận cha có tồn tại không
            Comment parent = commentRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bình luận cha để trả lời"));
            
            // Logic quan trọng: Bình luận cha phải thuộc cùng bài viết này
            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("Bình luận cha không thuộc bài viết này");
            }
        }
        // 3. Gán ID cho Comment
        comment.setPostId(post.getId());
        comment.setUserId(user.getId());
        
        // 4. Tăng số lượng comment trong Post
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        notificationService.sendNotification(
        user,
        post.getUser(),
        NotificationType.COMMENT_POST,
        post.getId()
    );

        // 5. Lưu Comment
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, Comment newData) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        // Có thể thêm kiểm tra quyền: Chỉ chủ comment mới được sửa
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        
        if (!comment.getUserId().equals(currentUser.getId())) {
             throw new RuntimeException("Không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(newData.getContent());
        
        return commentRepository.save(comment);
    }

@Transactional
    public void deleteComment(Long commentId) {
        // 1. Tìm comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        // 2. Tìm bài viết liên quan (để check quyền chủ bài viết và trừ số lượng)
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 3. Lấy User hiện tại đang thực hiện thao tác xóa
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 4. KIỂM TRA QUYỀN (Logic mới)
        boolean isOwnerOfComment = comment.getUserId().equals(currentUser.getId());
        boolean isOwnerOfPost = post.getUserId().equals(currentUser.getId());

        if (!isOwnerOfComment && !isOwnerOfPost) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này"); // Hoặc AccessDeniedException
        }
        // 4. LOGIC XÓA (CÁCH 1: Xóa cha bay luôn con)
        
        // Tìm danh sách các bình luận con (Replies)
        List<Comment> replies = commentRepository.findAllByParentCommentId(commentId);
        
        // Tính tổng số lượng comment sẽ bị xóa (1 cha + n con)
        int totalDeleted = 1 + replies.size();

        // Xóa danh sách con trước
        if (!replies.isEmpty()) {
            commentRepository.deleteAll(replies);
        }
        
        // Sau đó xóa cha
        commentRepository.delete(comment);
        if (post != null) {
        // Lấy số lượng cũ trừ đi tổng số đã xóa
        int newCount = Math.max(post.getCommentCount() - totalDeleted, 0);
        
        post.setCommentCount(newCount);
        postRepository.save(post);
    }
    }
}