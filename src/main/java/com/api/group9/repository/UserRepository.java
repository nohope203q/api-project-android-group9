package com.api.group9.repository;

import com.api.group9.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm User theo Username
    Optional<User> findByUsername(String username);

    // Tìm User theo Email
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findById(Long id);

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY u.username ASC
    """)
    List<User> suggestUsers(String q, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id <> :myId ORDER BY RAND()") 
    List<User> findRandomUsers(@Param("myId") Long myId, Pageable pageable);
    
}