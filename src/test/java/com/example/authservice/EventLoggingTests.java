package com.example.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventLoggingTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventLogRepository logRepository;
    @Autowired
    JwtService jwtService;

    @Test
    void loginCreatesEventLog() throws Exception {
        User user = new User();
        user.setUsername("logger");
        user.setPassword("pass");
        user.getRoles().add(Role.USER);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"logger\",\"password\":\"pass\"}"))
                .andExpect(status().isOk());

        assertThat(logRepository.findAll().stream()
                .anyMatch(l -> l.getUsername().equals("logger") && l.getEventType() == EventType.LOGIN)).isTrue();
    }

    @Test
    void accessCreatesEventLog() throws Exception {
        User user = new User();
        user.setUsername("jack");
        user.setPassword("pass");
        user.getRoles().add(Role.USER);
        userRepository.save(user);
        String token = jwtService.generateAccessToken(user);

        mockMvc.perform(get("/secret").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(logRepository.findAll().stream()
                .anyMatch(l -> l.getUsername().equals("jack") && l.getEventType() == EventType.ACCESS)).isTrue();
    }
}
