package com.api.group9.repository;

import com.api.group9.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

// Nhớ là Interface nhé, không phải Class
public interface StoryRepository extends JpaRepository<Story, Long> {

    @Query("SELECT s FROM Story s WHERE s.user.id IN :userIds AND s.expiredAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStories(@Param("userIds") List<Long> userIds, @Param("now") LocalDateTime now);
}