package com.api.group9.security;

import com.api.group9.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Trích xuất Token
        jwt = authHeader.substring(7);
        try {
            // --- SỬA QUAN TRỌNG Ở ĐÂY ---
            // Phải dùng extractEmail để lấy từ Claim "email"
            // Nếu dùng extractUsername nó sẽ ra ID -> Sai logic loadUser
            userEmail = jwtService.extractEmail(jwt); 
            
        } catch (Exception e) {
            // Nếu token lỗi format hoặc hết hạn thì cho qua luôn (để Spring Security xử lý 403 sau)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Xác thực
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Load User từ DB bằng EMAIL (Database tìm cột email)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Kiểm tra token hợp lệ
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}