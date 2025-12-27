package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.group9.dto.Respone.UserProfileResponse;
import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{identifier}")
    public ResponseEntity<?> getUserProfile(@PathVariable String identifier) {

        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user nào trùng khớp bro ơi!"));
        return ResponseEntity.ok().body(new UserProfileResponse(user));
    }
}
