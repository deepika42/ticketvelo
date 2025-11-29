package com.deepika.ticketvelo.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // 1. Inject from application.yml
    private String secretString;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key key;

    // 2. Initialize the Key AFTER injection happens
    @PostConstruct
    public void init() {
        // We decode the hex/base64 string or just use bytes
        byte[] keyBytes = secretString.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Invalid Token");
        }
    }
}