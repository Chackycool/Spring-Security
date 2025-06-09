package com.example.authservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

import com.example.authservice.EventLog;
import com.example.authservice.EventType;
import com.example.authservice.TokenBlacklistService;
import com.example.authservice.EventLogRepository;
import com.example.authservice.UserBlacklistService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final EventLogRepository eventLogRepository;
    private final MfaService mfaService;
    private final UserBlacklistService userBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.getRoles().add(Role.USER);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.isBlocked() || userBlacklistService.isBlacklisted(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (user.getRoles().contains(Role.ADMIN) && user.getMfaSecret() == null) {
            String secret = mfaService.generateSecret();
            user.setMfaSecret(secret);
            userRepository.save(user);
            logEvent(user.getUsername(), EventType.LOGIN);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MfaResponse(secret));
        }
        if (user.getMfaSecret() != null) {
            if (request.mfaCode() == null || !mfaService.verifyCode(user.getMfaSecret(), request.mfaCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        logEvent(user.getUsername(), EventType.LOGIN);
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/enable-mfa")
    public ResponseEntity<MfaResponse> enableMfa(org.springframework.security.core.Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getMfaSecret() == null) {
            String secret = mfaService.generateSecret();
            user.setMfaSecret(secret);
            userRepository.save(user);
        }
        return ResponseEntity.ok(new MfaResponse(user.getMfaSecret()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest request) {
        if (!jwtService.validate(request.refreshToken()) || blacklistService.isRevoked(request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = jwtService.extractUsername(request.refreshToken());
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.isBlocked() || userBlacklistService.isBlacklisted(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.getMfaSecret() != null) {
            if (request.mfaCode() == null || !mfaService.verifyCode(user.getMfaSecret(), request.mfaCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        logEvent(username, EventType.LOGIN);
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        if (!jwtService.validate(request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = jwtService.extractUsername(request.refreshToken());
        blacklistService.revokeToken(request.refreshToken(), jwtService.extractExpiration(request.refreshToken()));
        logEvent(username, EventType.LOGOUT);
        return ResponseEntity.ok().build();
    }

    public record RegisterRequest(String username, String password) {}
    public record LoginRequest(String username, String password, String mfaCode) {}
    public record LoginResponse(String accessToken, String refreshToken) {}
    public record RefreshRequest(String refreshToken, String mfaCode) {}
    public record MfaResponse(String secret) {}

    private void logEvent(String username, EventType type) {
        EventLog log = new EventLog();
        log.setUsername(username);
        log.setEventType(type);
        log.setTimestamp(Instant.now());
        eventLogRepository.save(log);
    }
}
