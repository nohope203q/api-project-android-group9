package com.api.group9.controller;

import com.api.group9.dto.Request.*;
import com.api.group9.dto.Respone.*;
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

    @Autowired private AuthService authService;

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
        UserRespone user = authService.login(req);
        // Token đã được tạo trong service hoặc tạo ở đây tùy logic
        // Ở đây giả sử service trả về UserRespone có sẵn token
        return ResponseEntity.ok(new LoginResponse("success", "Login thành công", user.getAccessToken())); 
    }

    // 4. Xin lại OTP (Resend)
    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody EmailRequest req) {
        // Mặc định resend cho việc verify account, hoặc mầy có thể thêm field purpose vào EmailRequest
        authService.sendOtp(req.getEmail(), OtpCode.OtpPurpose.REGISTER); 
        return ResponseEntity.ok(Map.of("message", "Đã gửi lại OTP."));
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
        authService.processPasswordReset(req.getEmail(), req.getOtpCode(), req.getNewPassword(), OtpCode.OtpPurpose.FORGOT_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công."));
    }

    // 7. Đổi mật khẩu - Bước 1: Xin OTP (Dành cho user ĐÃ LOGIN)
    @PostMapping("/request-change-password")
    public ResponseEntity<?> requestChangePassword(Authentication authentication) {
        if (authentication == null) throw new RuntimeException("Chưa đăng nhập");
        String email = authentication.getName(); // Lấy email từ Token an toàn tuyệt đối
        String currentPrincipal = authentication.getName();
        System.out.println("DEBUG: Giá trị lấy từ Token là: " + currentPrincipal);
        authService.sendOtp(email, OtpCode.OtpPurpose.CHANGE_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "OTP đổi mật khẩu đã gửi."));
    }

    // 8. Đổi mật khẩu - Bước 2: Confirm
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ResetPasswordRequest req) {
        // Lưu ý: Client vẫn phải gửi email trong body để khớp với logic service, 
        // hoặc mầy có thể overload hàm service để chỉ nhận pass + code nếu muốn bảo mật tận răng.
        authService.processPasswordReset(req.getEmail(), req.getOtpCode(), req.getNewPassword(), OtpCode.OtpPurpose.CHANGE_PASSWORD);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công."));
    }
}