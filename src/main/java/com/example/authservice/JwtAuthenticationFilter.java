package com.example.authservice;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.example.authservice.TokenBlacklistService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final EventLogRepository eventLogRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.validate(token) && !blacklistService.isRevoked(token)) {
                String username = jwtService.extractUsername(token);
                userRepository.findByUsername(username).ifPresent(user -> {
                    if (user.isBlocked() || user.isForceLogout()) {
                        return;
                    }
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    EventLog log = new EventLog();
                    log.setUsername(username);
                    log.setEventType(EventType.ACCESS);
                    log.setTimestamp(java.time.Instant.now());
                    eventLogRepository.save(log);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
