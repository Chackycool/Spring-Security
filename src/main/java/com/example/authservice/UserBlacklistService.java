package com.example.authservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBlacklistService {
    private final BlacklistedUserRepository repository;

    public boolean isBlacklisted(String username) {
        return repository.existsByUsername(username);
    }

    public void add(String username) {
        if (!repository.existsByUsername(username)) {
            BlacklistedUser user = new BlacklistedUser();
            user.setUsername(username);
            repository.save(user);
        }
    }

    public void remove(String username) {
        repository.deleteByUsername(username);
    }

    public java.util.List<BlacklistedUser> list() {
        return repository.findAll();
    }
}
