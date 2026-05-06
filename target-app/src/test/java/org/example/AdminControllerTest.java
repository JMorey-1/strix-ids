package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    private static final String ADMIN_TOKEN = "Bearer admin-token";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminHome_WithValidToken_ShouldReturnAdminHome() throws Exception {
        // Test admin home
        mockMvc.perform(get("/admin")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin home"));
    }

    @Test
    void users_WithValidToken_ShouldReturnUserList() throws Exception {
        // Test users endpoint
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin user list"));
    }

    @Test
    void settings_WithValidToken_ShouldReturnSettings() throws Exception {
        // Test settings endpoint
        mockMvc.perform(get("/admin/settings")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin settings"));
    }

    @Test
    void reports_WithValidToken_ShouldReturnReports() throws Exception {
        // Test reports endpoint
        mockMvc.perform(get("/admin/reports")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin reports"));
    }

    @Test
    void audit_WithValidToken_ShouldReturnAuditLog() throws Exception {
        // Test audit endpoint
        mockMvc.perform(get("/admin/audit")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin audit log"));
    }

    @Test
    void updateSettings_WithValidToken_ShouldReturnUpdatedMessage() throws Exception {
        // Test update settings
        mockMvc.perform(post("/admin/settings")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"safe\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin settings updated"));
    }

    @Test
    void generateReport_WithValidToken_ShouldReturnReportStartedMessage() throws Exception {
        // Test generate report
        mockMvc.perform(post("/admin/reports")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"daily\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin report generation started"));
    }

    @Test
    void auditAction_WithValidToken_ShouldReturnAuditActionMessage() throws Exception {
        // Test audit action
        mockMvc.perform(post("/admin/audit")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"review\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin audit action recorded"));
    }

    @Test
    void adminHome_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        // Test missing token
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    void adminHome_WithWrongToken_ShouldReturnForbidden() throws Exception {
        // Test wrong token
        mockMvc.perform(get("/admin")
                        .header("Authorization", "Bearer wrong-token"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void adminHome_WithBlankToken_ShouldReturnUnauthorized() throws Exception {
        // Test blank token
        mockMvc.perform(get("/admin")
                        .header("Authorization", " "))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }
}