package com.api.group9.controller;

import com.api.group9.dto.Request.*;
import com.api.group9.dto.Response.*;
import com.api.group9.model.OtpCode;
import com.api.group9.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 1. Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(req));
    }

    // 2. Xác thực OTP (Chung)
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        boolean success = authService.verifyOtp(req);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Xác thực OTP thành công."));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "OTP sai hoặc hết hạn."));
    }

    // 3. Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        UserResponse user = authService.login(req);
        return ResponseEntity.ok(new LoginResponse("success", "Login thành công", user.getAccessToken(), user.getId()));
    }

    // 4. Xin lại OTP (Resend)
    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody EmailRequest req) {
        if (req.getPurpose() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn loại OTP (purpose)"));
        }

        authService.sendOtp(req.getEmail(), req.getPurpose());

        return ResponseEntity.ok(Map.of("message", "Đã gửi OTP cho mục đích: " + req.getPurpose()));
    }

    // 5. Quên mật khẩu - Bước 1: Xin OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest req) {
        authService.sendOtp(req.getEmail(), OtpCode.OtpPurpose.FORGOT_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "OTP khôi phục mật khẩu đã gửi tới email."));
    }

    // 6. Quên mật khẩu - Bước 2: Reset
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        authService.processPasswordReset(req.getEmail(), req.getOtpCode(), req.getNewPassword(),
                OtpCode.OtpPurpose.FORGOT_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Vui lòng đăng nhập."));
            }
            String currentEmail = authentication.getName();

            authService.changePassword(currentEmail, req);

            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}