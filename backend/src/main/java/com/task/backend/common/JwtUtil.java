package com.task.backend.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // 使用安全的密钥生成方式
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("your-secret-key-here-must-be-at-least-32-chars".getBytes());
    private final long EXPIRE_TIME = 86400000; // 过期时间：24小时 expire

    // 生成 Token
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)  // 新版用 .claims() 而不是 .setClaims()
                .issuedAt(now)   // 新版用 .issuedAt() 而不是 .setIssuedAt()
                .expiration(expireDate) // 新版用 .expiration() 而不是 .setExpiration()
                .signWith(SECRET_KEY, Jwts.SIG.HS256) // 新版签名方式
                .compact();
    }

    // 解析 Token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)  // 新版用 .verifyWith() 而不是 .setSigningKey()
                .build()
                .parseSignedClaims(token) // 新版用 .parseSignedClaims() 而不是 .parseClaimsJws()
                .getPayload();
    }

    // 验证 Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}