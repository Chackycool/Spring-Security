package com.example.authservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final TokenBlacklistService blacklistService;
    private final EventLogRepository eventLogRepository;
    private final UserRepository userRepository;
    private final UserBlacklistService userBlacklistService;

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

    @GetMapping("/events")
    public java.util.List<EventLog> events() {
        return eventLogRepository.findTop20ByOrderByTimestampDesc();
    }

    @GetMapping("/users")
    public java.util.List<User> users() {
        return userRepository.findAll();
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<User> addRole(@PathVariable Long id, @RequestBody RoleRequest request) {
        return userRepository.findById(id)
                .map(u -> {
                    u.getRoles().add(Role.valueOf(request.role()));
                    userRepository.save(u);
                    return ResponseEntity.ok(u);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    public ResponseEntity<User> removeRole(@PathVariable Long id, @PathVariable String role) {
        return userRepository.findById(id)
                .map(u -> {
                    u.getRoles().remove(Role.valueOf(role));
                    userRepository.save(u);
                    return ResponseEntity.ok(u);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<User> blockUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setBlocked(true);
                    userRepository.save(u);
                    return ResponseEntity.ok(u);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<User> unblockUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> {
                    u.setBlocked(false);
                    userRepository.save(u);
                    return ResponseEntity.ok(u);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/blacklist")
    public List<BlacklistedUser> listBlacklist() {
        return userBlacklistService.list();
    }

    @PostMapping("/blacklist")
    public ResponseEntity<Void> addBlacklist(@RequestBody UsernameRequest req) {
        userBlacklistService.add(req.username());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/blacklist/{username}")
    public ResponseEntity<Void> removeBlacklist(@PathVariable String username) {
        userBlacklistService.remove(username);
        return ResponseEntity.ok().build();
    }

    public record RoleRequest(String role) {}
    public record TokenRequest(String token) {}
    public record UsernameRequest(String username) {}
}
