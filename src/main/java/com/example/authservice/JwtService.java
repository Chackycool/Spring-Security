package com.example.authservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.time.Instant;

@Service
public class JwtService {

    private Key key;

    @PostConstruct
    void init() {
        // In a real application the secret should come from configuration
        this.key = Keys.hmacShaKeyFor("change-me-change-me-change-me-change-me".getBytes());
    }

    public String generateAccessToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getRoles())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 30 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + 7L * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public Instant extractExpiration(String token) {
        Date exp = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration();
        return exp.toInstant();
    }
}
