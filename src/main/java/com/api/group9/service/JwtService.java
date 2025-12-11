package com.api.group9.service;

import com.api.group9.dto.Respone.UserRespone;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(UserRespone user) {
        Key key = getSignInKey();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .subject(user.getId().toString()) 
            .issuedAt(now) 
            .expiration(expiryDate) 
            .signWith(key) 
            .compact();
    }
    
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Kiểm tra Token hết hạn
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Hàm chung để lấy Claims (Payload)
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
    
    // Lấy khóa ký
    private SecretKey getSignInKey() { 
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}