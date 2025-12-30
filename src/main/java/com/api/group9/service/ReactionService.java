package com.api.group9.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.api.group9.model.Post;
import com.api.group9.model.Reaction;
import com.api.group9.model.User;
import com.api.group9.enums.ReactionType; // Import Enum
import com.api.group9.repository.PostRepository;
import com.api.group9.repository.ReactionRepository;
import com.api.group9.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Reaction likePost(Long postId) {
        // 1. Lấy Post từ DB (để gán vào Reaction)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // 2. Lấy User hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // 3. Kiểm tra tồn tại bằng đối tượng (Entity)
        if (reactionRepository.existsByPostAndUser(post, user)) {
            throw new IllegalStateException("Already liked this post");
        }

        // 4. Tạo Reaction bằng Constructor mới & Enum
        Reaction reaction = new Reaction(post, user, ReactionType.LIKE);
        
        return reactionRepository.save(reaction);
    }

    @Transactional
    public void unlikePost(Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Cần lấy Post để tìm Reaction theo cặp (Post, User)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        // Tìm reaction dựa trên Entity
        Reaction reaction = reactionRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new NoSuchElementException("Reaction not found"));

        reactionRepository.delete(reaction);
    }

    public long countReactions(Long postId) {
        // JPA tự động hiểu PostId là tìm theo field id của object Post
        return reactionRepository.countByPostId(postId);
    }
}