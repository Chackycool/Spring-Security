package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MfaChallengeTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MfaService mfaService;


    @Test
    void challengeRequiresMfaOnRefresh() throws Exception {
        User user = new User();
        user.setUsername("challenged");
        user.setPassword(passwordEncoder.encode("pass"));
        user.setMfaSecret(mfaService.generateSecret());
        user.getRoles().add(Role.USER);
        userRepository.save(user);

        String code = mfaService.currentCode(user.getMfaSecret());
        String login = "{\"username\":\"challenged\",\"password\":\"pass\",\"mfaCode\":\"" + code + "\"}";
        var loginRes = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String refresh = new ObjectMapper()
                .readTree(loginRes).get("refreshToken").asText();

        user.setMfaChallenge(true);
        userRepository.save(user);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());

        code = mfaService.currentCode(user.getMfaSecret());
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\",\"mfaCode\":\"" + code + "\"}"))
                .andExpect(status().isOk());
    }
}
