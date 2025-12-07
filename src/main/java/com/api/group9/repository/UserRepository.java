package com.api.group9.repository;

import com.api.group9.model.User;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.Optional;

// Kế thừa từ JpaRepository và thay đổi kiểu ID từ String sang Long
public interface UserRepository extends JpaRepository<User, Long> {

    // Các phương thức Custom Query vẫn giữ nguyên (Spring Data tự hiểu)
    
    // Tìm User theo Username
    Optional<User> findByUsername(String username);

    // Tìm User theo Email
    Optional<User> findByEmail(String email);
}