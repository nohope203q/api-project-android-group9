package com.api.group9.controller;

import com.api.group9.service.AuthService;
import com.api.group9.dto.Request.LoginRequest;
import com.api.group9.dto.Request.RegisterRequest;
import com.api.group9.dto.Request.VerifyOtpRequest;
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
@RequestMapping("/api") 
public class AuthController {

    @Autowired
    private AuthService authService;
    // --- 1. API Đăng ký tài khoản (/api/register) ---
    // Request Body: RegisterRequest (username, email, password, fullName)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        try {
  
            RegisterResponse response = new RegisterResponse(
                "success",
                "Đăng ký thành công! Vui lòng xác thực OTP."
            );
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            RegisterResponse errorResponse = new RegisterResponse(
                "error",
                e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // --- 2. API Xác thực OTP (/api/verify-otp) ---
    // Request Body: VerifyOtpRequest (username, otpCode)
    // Response Body: VerifyOtpResponse (message)
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
    // Request Body: LoginRequest (username, password)
    // Response Body: LoginResponse (token hoặc user info)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            UserRespone loggedInUser = authService.login(req);
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Đăng nhập thành công!",
                "user", loggedInUser // Trả về thông tin User đã đăng nhập
            );
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = Map.of(
                "status", "error",
                "message", e.getMessage()
            );
            // 401 Unauthorized nếu đăng nhập thất bại
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}