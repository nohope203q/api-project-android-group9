package com.api.group9.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.api.group9.model.Reaction;
import com.api.group9.model.Post;
import com.api.group9.model.User;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    // Tìm theo Object Post và User
    boolean existsByPostAndUser(Post post, User user);
    
    Optional<Reaction> findByPostAndUser(Post post, User user);
    
    // Đếm số lượng (JPA tự hiểu PostId map với post.id)
    long countByPostId(Long postId);

    @Query("SELECT r.user FROM Reaction r WHERE r.post.id = :postId")
    List<User> findUsersByPostId(@Param("postId") Long postId);
}