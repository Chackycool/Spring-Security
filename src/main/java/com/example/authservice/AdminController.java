package com.example.authservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TokenBlacklistService blacklistService;
    private final EventLogRepository eventLogRepository;

    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@RequestBody TokenRequest request) {
        blacklistService.revokeToken(request.token(), Instant.now().plusSeconds(3600));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public Map<String, Long> dashboard() {
        long logins = eventLogRepository.countByEventType(EventType.LOGIN);
        long logouts = eventLogRepository.countByEventType(EventType.LOGOUT);
        long accesses = eventLogRepository.countByEventType(EventType.ACCESS);
        return Map.of("logins", logins, "logouts", logouts, "accesses", accesses);
    }

    public record TokenRequest(String token) {}
}
