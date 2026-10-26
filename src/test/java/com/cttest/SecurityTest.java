package com.cttest;

import com.cttest.model.dto.LoginRequest;
import com.cttest.model.entity.User;
import com.cttest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setAdmin(true);
        userRepository.save(admin);
    }

    @Test
    void publicEndpoints_ShouldNotRequireAuth() throws Exception {
        mockMvc.perform(post("/api/public/validate-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": 123456, \"studentId\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEndpoint_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/centers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postEndpoint_WithoutAuth_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequest login = new LoginRequest("admin", "admin");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        LoginRequest login = new LoginRequest("admin", "wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithValidToken_ShouldAccessProtectedEndpoint() throws Exception {
        LoginRequest login = new LoginRequest("admin", "admin");
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/centers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void login_WithInvalidToken_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/centers")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createQuestion_WithNoCorrectAnswer_ShouldReturn400() throws Exception {
        LoginRequest login = new LoginRequest("admin", "admin");
        String loginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(loginRes).get("token").asText();

        String invalidQuestionJson = "{\"question\":\"What is 1+1?\",\"answersLayout\":\"COLUMNS\",\"answers\":[{\"order\":1,\"correct\":false}]}";

        mockMvc.perform(post("/api/tests/1/questions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidQuestionJson))
                .andExpect(status().isBadRequest());
    }
}
