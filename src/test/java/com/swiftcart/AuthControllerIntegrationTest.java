package com.swiftcart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftcart.config.RedisTestMockConfig;
import com.swiftcart.dto.request.LoginRequest;
import com.swiftcart.dto.request.RegisterRequest;
import com.swiftcart.entity.Role;
import com.swiftcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the auth flow.
 * Requires a running PostgreSQL instance (or swap datasource for H2 in test profile).
 * Use @ActiveProfiles("test") + application-test.yml with H2 for CI/CD pipelines.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisTestMockConfig.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Smith");
        request.setEmail("alice@example.com");
        request.setPassword("securePass1");
        request.setRole(Role.BUYER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("BUYER"));
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice Smith");
        request.setEmail("alice@example.com");
        request.setPassword("securePass1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_withInvalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Bob");
        request.setEmail("not-an-email");
        request.setPassword("securePass1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void register_withShortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Bob");
        request.setEmail("bob@example.com");
        request.setPassword("short");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        // Register first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Charlie");
        registerRequest.setEmail("charlie@example.com");
        registerRequest.setPassword("myPassword9");
        registerRequest.setRole(Role.SELLER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Now login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("charlie@example.com");
        loginRequest.setPassword("myPassword9");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("SELLER"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Dave");
        registerRequest.setEmail("dave@example.com");
        registerRequest.setPassword("correctPassword1");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("dave@example.com");
        loginRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_withNonExistentUser_returns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("ghost@example.com");
        loginRequest.setPassword("somePassword1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_defaultsRoleToBuyer_whenRoleNotProvided() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Eve");
        request.setEmail("eve@example.com");
        request.setPassword("evePassword1");
        // Intentionally NOT setting role

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("BUYER"));
    }
}
