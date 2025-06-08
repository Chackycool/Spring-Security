package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MfaTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MfaService mfaService;

    @Test
    void mfaLoginRequiresCode() throws Exception {
        User user = new User();
        user.setUsername("mfauser");
        user.setPassword(passwordEncoder.encode("pass"));
        user.setMfaSecret(mfaService.generateSecret());
        user.getRoles().add(Role.USER);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"mfauser\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());

        String code = mfaService.currentCode(user.getMfaSecret());
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"mfauser\",\"password\":\"pass\",\"mfaCode\":\"" + code + "\"}"))
                .andExpect(status().isOk());
    }
}
