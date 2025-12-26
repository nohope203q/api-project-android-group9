package com.api.group9.service;

import com.api.group9.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // --- 1. SỬA QUAN TRỌNG: Lưu Email vào Subject ---
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail()) // <--- SỬA THÀNH GET EMAIL
                
                // Thêm ID và Tên vào payload để Frontend dùng nếu cần
                .claim("userId", user.getId())
                .claim("fullName", user.getFullName())
                
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // --- 2. Trích xuất Username (Email) ---
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- 3. Hàm kiểm tra token hợp lệ (Dùng cho Filter) ---
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Token đúng khi: Email trong token khớp với UserDetails VÀ chưa hết hạn
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi Token: " + e.getMessage());
        }
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- 4. Hàm trích xuất Subject (Email) để dùng trong Filter ---
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}