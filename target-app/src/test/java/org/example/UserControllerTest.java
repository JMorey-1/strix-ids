package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String USER_TOKEN = "Bearer user-token";
    private static final String ADMIN_TOKEN = "Bearer admin-token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void profile_WithUserToken_ShouldReturnUserProfile() throws Exception {
        // Test profile with user token
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User profile"));
    }

    @Test
    void profile_WithAdminToken_ShouldReturnUserProfile() throws Exception {
        // Test profile with admin token
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User profile"));
    }

    @Test
    void dashboard_WithUserToken_ShouldReturnUserDashboard() throws Exception {
        // Test dashboard with user token
        mockMvc.perform(get("/user/dashboard")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User dashboard"));
    }

    @Test
    void dashboard_WithAdminToken_ShouldReturnUserDashboard() throws Exception {
        // Test dashboard with admin token
        mockMvc.perform(get("/user/dashboard")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User dashboard"));
    }

    @Test
    void profile_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        // Test missing token
        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void profile_WithWrongToken_ShouldReturnForbidden() throws Exception {
        // Test wrong token
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", "Bearer wrong-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void profile_WithBlankToken_ShouldReturnUnauthorized() throws Exception {
        // Test blank token
        mockMvc.perform(get("/user/profile")
                        .header("Authorization", " "))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }
}