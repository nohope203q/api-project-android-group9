package com.api.group9.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; 
    private final String FROM_EMAIL = "MAIL_ACCOUNT"; 

    /**
     * Hàm gửi email cơ bản
     * @param to Địa chỉ email người nhận
     * @param subject Chủ đề email
     * @param body Nội dung email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("✅ Đã gửi email thành công đến: " + to);

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email đến " + to + ": " + e.getMessage());
        }
    }
    
}