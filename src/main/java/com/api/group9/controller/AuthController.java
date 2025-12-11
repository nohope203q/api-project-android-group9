package com.api.group9.controller;

import com.api.group9.service.AuthService;

import com.api.group9.dto.Request.LoginRequest;
import com.api.group9.dto.Request.RegisterRequest;
import com.api.group9.dto.Request.VerifyOtpRequest;
import com.api.group9.dto.Respone.LoginResponse;
import com.api.group9.dto.Respone.RegisterResponse;
import com.api.group9.dto.Respone.UserRespone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/auth") 
public class AuthController {

    @Autowired
    private AuthService authService;
    // --- 1. API Đăng ký tài khoản (/api/register) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        try {
            RegisterResponse response = authService.registerUser(user); 
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            RegisterResponse errorResponse = new RegisterResponse(
                "error",
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // --- 2. API Xác thực OTP (/api/verify-otp) ---
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        try {
            boolean success = authService.verifyOtp(req);

            if (success) {
                Map<String, String> response = Map.of("message", "Xác thực OTP thành công!");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> errorResponse = Map.of("message", "OTP không đúng hoặc đã hết hạn.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // --- 3. API Đăng nhập (/api/login) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            UserRespone loggedInUser = authService.login(req);
            String token = authService.generateToken(loggedInUser);
            LoginResponse response = new LoginResponse(
                "success",
                "Đăng nhập thành công!",
                token
            );
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    // ---4. generate otp---
    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.resendOTP(email);
            Map<String, String> response = Map.of(
                "status", "success",
                "message", "Mã OTP đã được gửi đến email."
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ---5. quen mat khau---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.sendOtpForForgotPassword(email);
            Map<String, String> response = Map.of(
                "status", "success",
                "message", "Mã OTP để lấy lại mật khẩu đã được gửi đến email."
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ---6. doi mat khau---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.sendOtpForPasswordChange(email);
            Map<String, String> response = Map.of(
                "status", "success",
                "message", "Mã OTP để đổi mật khẩu đã được gửi đến email."
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


}