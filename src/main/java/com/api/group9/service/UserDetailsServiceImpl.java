package com.api.group9.service;

import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Hàm được gọi bởi Spring Security để tải User dựa trên Subject (ID/Username)
     * Vì trong JWT ta lưu User ID, nên ta dùng ID để tìm
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Long id = Long.parseLong(userId); // Giả sử ID là Long

        com.api.group9.model.User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User với ID: " + userId));

        // Trả về đối tượng UserDetails mà Spring Security yêu cầu
        // Lưu ý: User.getUsername() ở đây không cần khớp với 'userId'
        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(), // User ID
                user.getPasswordHash(),  // PasswordHash
                Collections.emptyList()  // Danh sách Quyền (Authorities) - Tạm thời để trống
        );
    }
}