package org.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.mitigation.TargetMitigationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

  private static final String ADMIN_TOKEN = "Bearer admin-token";

  @MockitoBean private TargetMitigationService targetMitigationService;

  @Autowired private MockMvc mockMvc;

  @Test
  void adminHome_WithValidToken_ShouldReturnAdminHome() throws Exception {
    mockMvc
        .perform(get("/admin").header("Authorization", ADMIN_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin home"));
  }

  @Test
  void users_WithValidToken_ShouldReturnUserList() throws Exception {
    mockMvc
        .perform(get("/admin/users").header("Authorization", ADMIN_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin user list"));
  }

  @Test
  void settings_WithValidToken_ShouldReturnSettings() throws Exception {
    mockMvc
        .perform(get("/admin/settings").header("Authorization", ADMIN_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin settings"));
  }

  @Test
  void reports_WithValidToken_ShouldReturnReports() throws Exception {
    mockMvc
        .perform(get("/admin/reports").header("Authorization", ADMIN_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin reports"));
  }

  @Test
  void audit_WithValidToken_ShouldReturnAuditLog() throws Exception {
    mockMvc
        .perform(get("/admin/audit").header("Authorization", ADMIN_TOKEN))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin audit log"));
  }

  @Test
  void updateSettings_WithValidToken_ShouldReturnUpdatedMessage() throws Exception {
    mockMvc
        .perform(
            post("/admin/settings")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mode\":\"safe\"}"))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin settings updated"));
  }

  @Test
  void generateReport_WithValidToken_ShouldReturnReportStartedMessage() throws Exception {
    mockMvc
        .perform(
            post("/admin/reports")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"daily\"}"))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin report generation started"));
  }

  @Test
  void auditAction_WithValidToken_ShouldReturnAuditActionMessage() throws Exception {
    mockMvc
        .perform(
            post("/admin/audit")
                .header("Authorization", ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"review\"}"))
        .andExpect(status().isOk())
        .andExpect(content().string("Admin audit action recorded"));
  }

  @Test
  void adminHome_WithoutToken_ShouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get("/admin"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Unauthorized"));
  }

  @Test
  void adminHome_WithWrongToken_ShouldReturnForbidden() throws Exception {
    mockMvc
        .perform(get("/admin").header("Authorization", "Bearer wrong-token"))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Forbidden"));
  }

  @Test
  void adminHome_WithBlankToken_ShouldReturnUnauthorized() throws Exception {
    mockMvc
        .perform(get("/admin").header("Authorization", " "))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("Unauthorized"));
  }
}
