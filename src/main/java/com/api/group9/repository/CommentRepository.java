package com.api.group9.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.api.group9.model.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
}