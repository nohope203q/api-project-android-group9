package com.api.group9.repository;

import com.api.group9.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p FROM Post p " +
        "LEFT JOIN FETCH p.user " +  
        "WHERE p.userId IN :userIds " +
        "ORDER BY p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.userId IN :userIds")
Page<Post> findNewsFeed(@Param("userIds") List<Long> userIds, Pageable pageable);
}