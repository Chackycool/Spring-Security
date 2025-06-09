package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminForceLogoutTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void requireMfaSetsSecretAndForcesLogout() throws Exception {
        User user = new User();
        user.setUsername("target");
        user.setPassword(passwordEncoder.encode("pass"));
        user.getRoles().add(Role.USER);
        userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/require-mfa"))
                .andExpect(status().isOk());

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertThat(refreshed.getMfaSecret()).isNotNull();
        assertThat(refreshed.isForceLogout()).isTrue();
    }

    @Test
    void forceLogoutEndpointSetsFlag() throws Exception {
        User user = new User();
        user.setUsername("kick");
        user.setPassword(passwordEncoder.encode("pass"));
        user.getRoles().add(Role.USER);
        userRepository.save(user);

        mockMvc.perform(post("/admin/users/" + user.getId() + "/force-logout"))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(user.getId()).orElseThrow().isForceLogout()).isTrue();
    }
}
