package com.api.group9.service;

import com.api.group9.dto.Request.*;
import com.api.group9.dto.Respone.*;
import com.api.group9.model.OtpCode;
import com.api.group9.model.User;
import com.api.group9.repository.OtpCodeRepository;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private OtpCodeRepository otpCodeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private JwtService jwtService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    // --- LOGIC ĐĂNG KÝ & ĐĂNG NHẬP (Giữ nguyên, chỉ làm gọn) ---
    @Transactional
    public RegisterResponse registerUser(RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) throw new RuntimeException("Tên đăng nhập đã tồn tại.");
        if (userRepository.findByEmail(req.getEmail()).isPresent()) throw new RuntimeException("Email đã tồn tại.");

        User newUser = new User();
        newUser.setUsername(req.getUsername());
        newUser.setEmail(req.getEmail());
        newUser.setFullName(req.getFullName());
        newUser.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        newUser.setIsVerified(false);
        newUser.setCreatedAt(Instant.now());
        
        userRepository.save(newUser);
        generateAndSendOtp(newUser, "Xác thực đăng ký", OtpCode.OtpPurpose.REGISTER);
        
        return new RegisterResponse("success", "Đăng ký thành công. Kiểm tra email lấy OTP.");
    }

    public UserRespone login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Sai email hoặc mật khẩu."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai email hoặc mật khẩu.");
        }
        if (!user.getIsVerified()) {
            throw new RuntimeException("Tài khoản chưa xác thực OTP.");
        }

        String token = jwtService.generateToken(user);
        
        // Builder pattern hoặc Set thủ công (Nên dùng Mapper như MapStruct nếu dự án lớn)
        UserRespone res = new UserRespone();
        res.setAccessToken(token);
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setIsVerified(user.getIsVerified());
        return res;
    }

    // --- LOGIC OTP & PASSWORD (REFACTOR CỰC MẠNH) ---

    // 1. Hàm verify OTP chung
    public boolean verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        Optional<OtpCode> otpOpt = otpCodeRepository
                .findTopByUserAndCodeAndPurposeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
                        user, req.getOtpCode(), req.getPurpose(), Instant.now());

        if (otpOpt.isEmpty()) return false;

        OtpCode otp = otpOpt.get();
        
        // Nếu là REGISTER thì kích hoạt luôn tại đây
        if (otp.getPurpose() == OtpCode.OtpPurpose.REGISTER) {
            user.setIsVerified(true);
            userRepository.save(user);
            otp.setUsed(true); // Đốt OTP luôn
            otpCodeRepository.save(otp);
        }
        // Các purpose khác (Change pass/Forgot pass) thì chưa đốt OTP vội, để bước sau đốt.
        
        return true;
    }

    // 2. Hàm Gửi OTP chung (Dùng cho cả Forgot, Change Pass, Resend)
    public void sendOtp(String email, OtpCode.OtpPurpose purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống."));
        
        String subject = switch (purpose) {
            case FORGOT_PASSWORD -> "Mã xác thực QUÊN MẬT KHẨU";
            case CHANGE_PASSWORD -> "Mã xác thực ĐỔI MẬT KHẨU";
            case REGISTER -> "Mã xác thực ĐĂNG KÝ";
            default -> "Mã OTP của bạn";
        };
        
        generateAndSendOtp(user, subject, purpose);
    }

    // 3. Hàm Đổi Mật Khẩu chung (Gộp cả Reset và Change)
    public void processPasswordReset(String email, String otpCode, String newPassword, OtpCode.OtpPurpose purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        OtpCode otp = otpCodeRepository
                .findTopByUserAndCodeAndPurposeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
                        user, otpCode, purpose, Instant.now())
                .orElseThrow(() -> new RuntimeException("OTP không đúng hoặc đã hết hạn."));

        // Update Pass
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đốt OTP
        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    // --- PRIVATE HELPERS ---
    private void generateAndSendOtp(User user, String subject, OtpCode.OtpPurpose purpose) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        
        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCode(code);
        otp.setPurpose(purpose);
        otp.setExpiryTime(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        otp.setUsed(false);
        otpCodeRepository.save(otp);

        // Gửi mail async để không chặn luồng chính (nếu setup @Async)
        try {
            String body = "Mã OTP: " + code + "\nHết hạn sau 5 phút.";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }
}