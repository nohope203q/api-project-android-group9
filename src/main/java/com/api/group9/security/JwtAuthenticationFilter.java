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
        final String userEmail; // <-- Đổi tên biến cho đúng bản chất

        // 1. Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Trích xuất Token
        jwt = authHeader.substring(7);
        try {
            // Lấy EMAIL từ token (Thay vì ID như cũ)
            userEmail = jwtService.extractUsername(jwt); 
        } catch (RuntimeException e) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Xác thực
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Load User từ DB bằng EMAIL
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Dùng hàm isTokenValid (Public) thay vì isTokenExpired (Private)
            // Hàm này kiểm tra cả hạn sử dụng lẫn tính chính chủ
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