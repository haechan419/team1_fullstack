package com.smartspend.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
@Log4j2
public class JWTUtil {

    @Value("${jwt.secret:smartspend-secret-key-for-jwt-token-generation-minimum-256-bits}")
    private String secret;

    @Value("${jwt.expiration:86400}") // 24시간 (초)
    private int expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String role) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expirationTime = now.plusSeconds(expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(expirationTime.toInstant()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long validateAndExtractUserId(String tokenStr) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(tokenStr)
                    .getBody()
                    .getSubject();

            return Long.parseLong(subject);
        } catch (JwtException | NumberFormatException e) {
            log.error("JWT 검증 실패: {}", e.getMessage());
            return null; // 필터에서 null 체크를 하므로 null 반환
        }
    }

    public String extractRole(String tokenStr) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(tokenStr)
                    .getBody()
                    .get("role", String.class);
        } catch (JwtException e) {
            log.error("JWT 역할 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}

