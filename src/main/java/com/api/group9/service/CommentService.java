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

    public Page<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        return commentPage.map(this::mapToResponse);
    }

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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));
        if (comment.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bình luận cha để trả lời"));

            if (!parent.getPostId().equals(postId)) {
                throw new IllegalArgumentException("Bình luận cha không thuộc bài viết này");
            }
            if (!parent.getUserId().equals(user.getId())) {
                User parentAuthor = userRepository.findById(parent.getUserId()).orElse(null);
                
                if (parentAuthor != null) {
                    notificationService.sendNotification(
                            user,       
                            parentAuthor,   
                            NotificationType.COMMENT_REPLY, 
                            post.getId()       
                    );
                }
            }
        }
        comment.setPostId(post.getId());
        comment.setUserId(user.getId());

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        notificationService.sendNotification(
                user,
                post.getUser(),
                NotificationType.COMMENT_POST,
                post.getId());

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, Comment newData) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        boolean isOwnerOfComment = comment.getUserId().equals(currentUser.getId());
        boolean isOwnerOfPost = post.getUserId().equals(currentUser.getId());

        if (!isOwnerOfComment && !isOwnerOfPost) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này"); // Hoặc AccessDeniedException
        }

        List<Comment> replies = commentRepository.findAllByParentCommentId(commentId);

        int totalDeleted = 1 + replies.size();

        if (!replies.isEmpty()) {
            commentRepository.deleteAll(replies);
        }

        commentRepository.delete(comment);
        if (post != null) {
            int newCount = Math.max(post.getCommentCount() - totalDeleted, 0);

            post.setCommentCount(newCount);
            postRepository.save(post);
        }
    }
}