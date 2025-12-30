package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public List<Comment> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }
        return commentRepository.findByPostId(postId);
    }

    @Transactional
    public Comment addComment(Long postId, Comment comment) {
        // 1. Kiểm tra Post có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 2. Lấy User từ Token (SỬA: Token chứa Email, phải tìm bằng Email)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found with email: " + email));

        // 3. Gán ID cho Comment (SỬA: Gán ID thay vì gán Object)
        comment.setPostId(post.getId());
        comment.setUserId(user.getId());
        
        // 4. Tăng số lượng comment trong Post
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        // 5. Lưu Comment
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long commentId, Comment newData) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        comment.setContent(newData.getContent());
        // Không cần setUpdatedAt vì @PreUpdate trong Entity tự làm
        
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));

        // SỬA: Entity Comment chỉ lưu postId (Long), không getPost() được
        // Phải tìm Post thủ công để trừ số lượng
        Post post = postRepository.findById(comment.getPostId()).orElse(null);

        if (post != null) {
            int newCount = Math.max(post.getCommentCount() - 1, 0);
            post.setCommentCount(newCount);
            postRepository.save(post);
        }

        commentRepository.delete(comment);
    }
}