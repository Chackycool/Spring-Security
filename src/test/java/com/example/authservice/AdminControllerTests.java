package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    TokenBlacklistService blacklistService;
    @Autowired
    RevokedTokenRepository revokedTokenRepository;

    @Test
    void adminRevokeEndpointAddsToken() throws Exception {
        String token = "sample";
        mockMvc.perform(post("/admin/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());

        assertThat(revokedTokenRepository.existsByToken(token)).isTrue();
    }
}
