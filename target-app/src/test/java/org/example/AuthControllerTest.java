package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_WithValidUserCredentials_ShouldReturnUserLoginSuccessful() throws Exception {
        // Test valid user login
        String requestBody = """
                {
                    "username": "user1",
                    "password": "pass123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("User login successful"));
    }

    @Test
    void login_WithValidAdminCredentials_ShouldReturnAdminLoginSuccessful() throws Exception {
        // Test valid admin login
        String requestBody = """
                {
                    "username": "admin",
                    "password": "admin123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin login successful"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Test wrong login
        String requestBody = """
                {
                    "username": "user1",
                    "password": "wrongpass"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void login_WithMissingUsername_ShouldReturnBadRequest() throws Exception {
        // Test missing username
        String requestBody = """
                {
                    "password": "pass123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing username or password"));
    }

    @Test
    void login_WithMissingPassword_ShouldReturnBadRequest() throws Exception {
        // Test missing password
        String requestBody = """
                {
                    "username": "user1"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing username or password"));
    }

    @Test
    void login_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // Test empty body
        String requestBody = "{}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Missing username or password"));
    }

    @Test
    void logout_ShouldReturnLoggedOut() throws Exception {
        // Test logout
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));
    }
}
