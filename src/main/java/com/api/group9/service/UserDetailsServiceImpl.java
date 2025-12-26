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
     * Hàm được gọi bởi Spring Security để tải User
     * SỬA ĐỔI: Bây giờ Token lưu EMAIL, nên ta tìm bằng EMAIL
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Không cần parseLong nữa, vì đầu vào là Email (String)
        
        // 2. Tìm User trong DB bằng Email
        com.api.group9.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User với email: " + email));

        // 3. Trả về đối tượng UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),       // Dùng Email làm username xác thực
                user.getPasswordHash(), 
                Collections.emptyList() // Quyền hạn (Authorities)
        );
    }
}