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

    // --- 1. SỬA LẠI: Lưu ID vào Subject, Email đẩy xuống Claims ---
    public String generateToken(User user) {
        return Jwts.builder()
                // CHỖ NÀY QUAN TRỌNG: Đổi thành ID (ép kiểu về String)
                .subject(String.valueOf(user.getId())) 
                
                // Lưu thêm Email vào claims để sau này validate (đối chiếu)
                .claim("email", user.getEmail()) 
                
                // Các info phụ khác
                .claim("fullName", user.getFullName())
                
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    // --- 2. Hàm lấy User ID (từ Subject) ---
    // Dùng cái này khi mầy muốn biết "Ai đang gọi API"
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // --- 3. Hàm lấy Email (từ Claims) ---
    // Dùng cái này để phục vụ logic Validate bên dưới
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    // --- 4. SỬA LOGIC VALIDATE ---
    public boolean isTokenValid(String token, UserDetails userDetails) {
        // Lấy email từ trong token ra (lấy từ claim 'email' mình đã lưu ở trên)
        final String emailInToken = extractEmail(token);
        
        // So sánh Email trong token với Email trong Database (UserDetails)
        // Mầy vẫn dùng email để verify vì UserDetails của Spring thường lưu username là email
        return (emailInToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
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
}