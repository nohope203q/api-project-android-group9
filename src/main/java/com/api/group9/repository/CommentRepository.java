package com.api.group9.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.api.group9.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    List<Comment> findAllByParentCommentId(Long parentCommentId);
    void deleteByPostId(Long postId);

}
