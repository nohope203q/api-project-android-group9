package com.api.group9.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.api.group9.dto.Respone.UserRespone;
import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    public UserRespone getUserProfile(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        User user = userOpt.get();

        UserRespone userRespone = new UserRespone();
        userRespone.setId(user.getId());
        userRespone.setUsername(user.getUsername());
        userRespone.setEmail(user.getEmail());
        userRespone.setFullName(user.getFullName());
        userRespone.setProfilePictureUrl(user.getProfilePictureUrl());
        userRespone.setIsVerified(user.getIsVerified());
        userRespone.setBio(user.getBio());

        return userRespone;
    }
    
}
