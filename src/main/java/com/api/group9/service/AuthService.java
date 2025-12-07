package com.api.group9.service;

import com.api.group9.model.User;
import com.api.group9.repository.UserRepository;
import com.api.group9.dto.Request.LoginRequest;
import com.api.group9.dto.Request.RegisterRequest;
import com.api.group9.dto.Request.VerifyOtpRequest;
import com.api.group9.dto.Respone.UserRespone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired 
    private EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    public String registerUser(RegisterRequest req) {
    // 1. Kiểm tra username/email đã tồn tại chưa
    if (userRepository.findByUsername(req.getUsername()).isPresent()
        || userRepository.findByEmail(req.getEmail()).isPresent()) {
        throw new RuntimeException("Tên đăng nhập hoặc Email đã tồn tại.");
    }
    
    User newUser = new User();
    newUser.setUsername(req.getUsername());
    newUser.setEmail(req.getEmail());
    newUser.setFullName(req.getFullName()); 

    // Mã hóa mật khẩu
    String encodedPassword = passwordEncoder.encode(req.getPassword());
    newUser.setPasswordHash(encodedPassword); 
    
    // Thiết lập trạng thái ban đầu
    newUser.setIsVerified(false); 
    newUser.setCreatedAt(LocalDateTime.now()); 
    newUser.setUpdatedAt(LocalDateTime.now()); 

    // 4. Lưu User vào CSDL (Cần lưu trước khi gửi mail để đảm bảo data có trong DB)
    userRepository.save(newUser);

    // 5. TẠO VÀ GỬI OTP QUA EMAIL (BƯỚC BỔ SUNG)
    // Gọi hàm hỗ trợ đã tạo để tạo OTP, lưu vào DB và gửi email
    String otp = generateAndSendOtp(newUser, "Mã xác thực Đăng Ký tài khoản của bạn"); 

    return "Đăng ký thành công. OTP đã được gửi qua email: " + otp;
    // Hoặc chỉ cần return otp; nếu controller cần nó.
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
        LocalDateTime now = LocalDateTime.now();

        if (user.getOtpCode() == null || !user.getOtpCode().equals(req.getOtpCode())) {
            return false; // OTP sai
        }

        if (user.getOtpExpiry().isBefore(now)) {
            return false; // OTP hết hạn
        }

        // 2. Xác thực thành công: Cập nhật trạng thái và xóa OTP
        user.setIsVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        
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

        // 1. Kiểm tra mật khẩu (So sánh password nhập vào với password đã mã hóa)
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng.");
        }

        // 2. Kiểm tra đã xác thực OTP chưa
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

        // 3. Đăng nhập thành công, trả về User (hoặc Token JWT)
        return userRespone;
    }

    public String sendOtpForForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Nên trả về thông báo chung chung để tránh bị lộ thông tin email nào đã đăng ký
            // Nhưng trong ví dụ này, cứ ném lỗi cho dễ debug.
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        User user = userOpt.get();
        
        // Tạo và Gửi OTP
        generateAndSendOtp(user, "Mã xác thực QUÊN MẬT KHẨU của bạn");

        return "OTP để lấy lại mật khẩu đã được gửi đến email của bạn.";
    }

    /**
     * Hàm Gửi Lại OTP cho luồng Đổi Mật Khẩu (Khi Đang Đăng Nhập)
     * Thường dùng để xác thực trước khi cho user đổi mật khẩu mới.
     */
    public String sendOtpForPasswordChange(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        User user = userOpt.get();
        
        // Tạo và Gửi OTP
        generateAndSendOtp(user, "Mã xác thực ĐỔI MẬT KHẨU của bạn");

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

    private String generateAndSendOtp(User user, String subject) {
        // 1. Tạo OTP mới
        String otp = generateOtp();

        // 2. Cập nhật user với OTP và thời gian hết hạn
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        user.setUpdatedAt(LocalDateTime.now()); 
        userRepository.save(user);
        
        // 3. Gửi Email
        try {
            String body = "Chào " + user.getFullName() + ", \n\n" // Thêm dòng trống để dễ đọc
            + "Mã OTP của bạn là: " + otp + ". \n" // Xuống dòng
            + "**Vui lòng không chia sẻ mã này cho bất kỳ ai.** \n\n" // Thêm câu cảnh báo
            + "Mã này sẽ hết hạn sau " + OTP_EXPIRY_MINUTES + " phút. \n\n" 
            + "Thanks,\n"
            + "Team Group 9";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            // Log lỗi để debug
            System.err.println("Lỗi khi gửi email xác thực: " + e.getMessage());
            throw new RuntimeException("Lưu tài khoản thành công nhưng không gửi được email xác thực.", e);
        }

        return otp;
    }
}