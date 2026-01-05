package com.api.group9.repository;

import com.api.group9.model.User;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm User theo Username
    Optional<User> findByUsername(String username);

    // Tìm User theo Email
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
    
}