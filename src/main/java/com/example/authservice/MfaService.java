package com.example.authservice;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;

@Service
public class MfaService {
    private TimeBasedOneTimePasswordGenerator totp;

    @PostConstruct
    void init() {
        this.totp = new TimeBasedOneTimePasswordGenerator();
    }

    public String generateSecret() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(totp.getAlgorithm());
            keyGenerator.init(160);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean verifyCode(String secret, String code) {
        try {
            Key key = new SecretKeySpec(Base64.getDecoder().decode(secret), totp.getAlgorithm());
            String expected = totp.generateOneTimePasswordString(key, Instant.now());
            return expected.equals(code);
        } catch (Exception e) {
            return false;
        }
    }

    public String currentCode(String secret) {
        try {
            Key key = new SecretKeySpec(Base64.getDecoder().decode(secret), totp.getAlgorithm());
            return totp.generateOneTimePasswordString(key, Instant.now());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
