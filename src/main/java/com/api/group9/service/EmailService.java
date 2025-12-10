package com.api.group9.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;
    
    private final String FROM_ADDRESS = "thienhao.huynh.infosec@gmail.com"; 

    public void sendEmail(String to, String subject, String body) throws RuntimeException {
        
        // 1. Định nghĩa From, To, và Content
        Email from = new Email(FROM_ADDRESS);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", body); 
        Mail mail = new Mail(from, subject, toEmail, content);

        // 2. Khởi tạo SendGrid Client và Request
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // 3. Thực hiện gọi HTTP API
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Đã gửi email thành công qua SendGrid API đến: " + to);
            } else {
                // Lỗi API 
                String errorBody = response.getBody() != null ? response.getBody() : "Unknown Error";
                System.err.println("❌ Lỗi SendGrid API. Status: " + response.getStatusCode() + ". Response: " + errorBody);
                throw new RuntimeException("Lỗi API SendGrid: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi trong quá trình giao tiếp với SendGrid: " + e.getMessage());
            throw new RuntimeException("Lỗi gửi email qua SendGrid.", e); 
        }
    }
    
}