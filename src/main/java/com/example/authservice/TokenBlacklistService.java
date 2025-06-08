package com.example.authservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository repository;

    public void revokeToken(String token, Instant expiresAt) {
        RevokedToken revoked = new RevokedToken();
        revoked.setToken(token);
        revoked.setExpiresAt(expiresAt);
        repository.save(revoked);
    }

    public boolean isRevoked(String token) {
        return repository.existsByToken(token);
    }
}
