package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTests {

    @Autowired
    JwtService jwtService;

    @Test
    void generatedTokenContainsUsername() {
        User user = new User();
        user.setUsername("alice");
        String token = jwtService.generateAccessToken(user);
        assertThat(jwtService.validate(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }
}
