package com.api.group9.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.group9.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByContentContaining(String content);
}