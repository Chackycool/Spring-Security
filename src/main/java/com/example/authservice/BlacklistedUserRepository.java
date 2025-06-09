package com.example.authservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedUserRepository extends JpaRepository<BlacklistedUser, Long> {
    boolean existsByUsername(String username);
    void deleteByUsername(String username);
}
