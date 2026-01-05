package com.api.group9.config;

import com.api.group9.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                System.out.println(">>> 1. Đang kiểm tra kết nối WebSocket...");
                
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    
                    System.out.println(">>> 2. Token nhận được: " + token.substring(0, 10) + "...");

                    // --- SỬA CHỖ NÀY ---
                    // Dùng extractEmail để lấy email từ claim "email" thay vì lấy từ subject (ID)
                    String userEmail = jwtService.extractEmail(token); 
                    System.out.println(">>> 3. Email trong token: " + userEmail);

                    if (userEmail != null) {
                        // Load user từ DB bằng Email (cái này vẫn giữ nguyên)
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                        System.out.println(">>> 5. Tìm thấy User: " + userDetails.getUsername());

                        if (jwtService.isTokenValid(token, userDetails)) {
                            
                            // Tạo đối tượng xác thực
                            // Lưu ý: Param đầu tiên (Principal) nên để là userDetails hoặc userEmail
                            UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            System.out.println(">>> THÀNH CÔNG: User " + userEmail + " đã được cấp quyền WebSocket!");
                        } else {
                            System.out.println(">>> Token không hợp lệ!");
                            return null;
                        }
                    }
                } else {
                    // Không có header Authorization
                    System.out.println(">>> WebSocket: Không tìm thấy Header Authorization");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace(); 
                return null;
            }
        }
        return message;
    }
}