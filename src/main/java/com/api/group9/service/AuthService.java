package com.api.group9.service;

import com.api.group9.model.OtpCode;
import com.api.group9.model.User;
import com.api.group9.repository.OtpCodeRepository;
import com.api.group9.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import com.api.group9.dto.Request.LoginRequest;
import com.api.group9.dto.Request.RegisterRequest;
import com.api.group9.dto.Request.VerifyOtpRequest;
import com.api.group9.dto.Respone.RegisterResponse;
import com.api.group9.dto.Respone.UserRespone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.security.Key;



@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private EmailService emailService;


    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
    * Hàm đăng ký tài khoản
    */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest req) {
    if (userRepository.findByUsername(req.getUsername()).isPresent()) {
        throw new RuntimeException("Tên đăng nhập đã tồn tại.");
    }
    if (userRepository.findByEmail(req.getEmail()).isPresent()) {
        throw new RuntimeException("Email đã tồn tại.");
    }
    
    User newUser = new User();
    newUser.setUsername(req.getUsername());
    newUser.setEmail(req.getEmail());
    newUser.setFullName(req.getFullName()); 

    String encodedPassword = passwordEncoder.encode(req.getPassword());
    newUser.setPasswordHash(encodedPassword); 
    
    newUser.setIsVerified(false); 
    newUser.setCreatedAt(Instant.now()); 
    newUser.setUpdatedAt(Instant.now()); 

    userRepository.save(newUser);

    generateAndSendOtp(newUser, "Mã xác thực Đăng Ký tài khoản của bạn", OtpCode.OtpPurpose.REGISTER); 
    return new RegisterResponse("success", "Đăng ký thành công. OTP đã được gửi qua email");
}
    /**
     * Hàm xác thực OTP
     */
    public boolean verifyOtp(VerifyOtpRequest req) {
    
    Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

    if (userOpt.isEmpty()) {
        throw new RuntimeException("Người dùng không tồn tại.");
    }

    User user = userOpt.get();
    
    // 1. TÌM OTP HỢP LỆ NHẤT
    // Hàm này tìm mã OTP MỚI NHẤT, còn hạn, chưa sử dụng, khớp với code và user
    Optional<OtpCode> otpOpt = otpCodeRepository.findTopByUserAndCodeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
        user, 
        req.getOtpCode(), 
        Instant.now() 
    );

    if (otpOpt.isEmpty()) {
         // Nếu không tìm thấy: OTP sai, hoặc đã hết hạn, hoặc đã được sử dụng
         return false; 
    }

    OtpCode otp = otpOpt.get();

    // 2. XÁC THỰC THÀNH CÔNG: Cập nhật trạng thái User và vô hiệu hóa OTP
    
    // Chỉ cập nhật trạng thái đã xác thực khi OTP dùng cho ĐĂNG KÝ
    if (otp.getPurpose() == OtpCode.OtpPurpose.REGISTER) { 
        user.setIsVerified(true);
        userRepository.save(user);
    }
    
    // Đánh dấu OTP là đã sử dụng (Quan trọng để ngăn chặn Replay Attack)
    otp.setUsed(true); 
    otpCodeRepository.save(otp);
    
    return true;
}

    /**
     * Hàm đăng nhập
     */
    public UserRespone login(LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực OTP. Vui lòng kiểm tra email.");
        }

        UserRespone userRespone = new UserRespone();
        userRespone.setId(user.getId());
        userRespone.setUsername(user.getUsername());
        userRespone.setEmail(user.getEmail());
        userRespone.setFullName(user.getFullName());
        userRespone.setProfilePictureUrl(user.getProfilePictureUrl());
        userRespone.setIsVerified(user.getIsVerified());

        return userRespone;
    }

    public String sendOtpForForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        User user = userOpt.get();
        
        generateAndSendOtp(user, "Mã xác thực QUÊN MẬT KHẨU của bạn", OtpCode.OtpPurpose.FORGOT_PASSWORD);

        return "OTP để lấy lại mật khẩu đã được gửi đến email của bạn.";
    }

    public String sendOtpForPasswordChange(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        User user = userOpt.get();
        
        generateAndSendOtp(user, "Mã xác thực ĐỔI MẬT KHẨU của bạn", OtpCode.OtpPurpose.CHANGE_PASSWORD);

        return "OTP để đổi mật khẩu đã được gửi đến email của bạn.";
    }

    /**
     * Hàm tạo mã OTP ngẫu nhiên (6 chữ số)
     */
    private String generateOtp() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000; // Số từ 100000 đến 999999
        return String.valueOf(number);
    }

    private String generateAndSendOtp(User user, String subject, OtpCode.OtpPurpose purpose) {
        String otpCode = generateOtp();

        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCode(otpCode);
        otp.setPurpose(purpose);
        otp.setCreatedAt(Instant.now());
        otp.setExpiryTime(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES)); // Cần import ChronoUnit
        otp.setUsed(false);
        
        otpCodeRepository.save(otp);

        try {
            String body = "Chào " + user.getFullName() + ", \n\n" 
            + "Mã OTP của bạn là: " + otpCode + ". \n" 
            + "**Vui lòng không chia sẻ mã này cho bất kỳ ai.** \n\n"
            + "Mã này sẽ hết hạn sau " + OTP_EXPIRY_MINUTES + " phút. \n\n" 
            + "Thanks,\n"
            + "Team Group 9";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xác thực: " + e.getMessage());
            throw new RuntimeException("Lưu tài khoản thành công nhưng không gửi được email xác thực.", e);
        }

        return otpCode;
    }

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;


    public String generateToken(UserRespone user) {
    Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    return Jwts.builder()
        .subject(user.getId().toString()) 
        .issuedAt(now) 
        .expiration(expiryDate) 
        .signWith(key) 
        .compact();
    }

}